package com.turno.los.service.impl;

import com.turno.los.dto.SubmitLoanRequest;
import com.turno.los.entity.*;
import com.turno.los.entity.enums.LoanStatus;
import com.turno.los.repository.AgentRepository;
import com.turno.los.repository.CustomerRepository;
import com.turno.los.repository.LoanRepository;
import com.turno.los.service.LoanService;
import com.turno.los.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    @Override
    public Loan createLoan(Loan loan) {
        return loanRepo.save(loan);
    }

    private final CustomerRepository customerRepo;
    private final AgentRepository agentRepo;
    private final LoanRepository loanRepo;
    private final NotificationService notifications;
    private final JdbcTemplate jdbc;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Loan submit(SubmitLoanRequest req) {
        Customer customer = customerRepo.findByPhone(req.customerPhone)
                .orElseGet(() -> customerRepo.save(Customer.builder()
                        .name(req.customerName).phone(req.customerPhone).build()));

        Loan loan = Loan.builder()
                .loanPublicId(req.loanId)
                .customer(customer)
                .loanAmount(req.amount)
                .loanType(req.type)
                .applicationStatus(LoanStatus.APPLIED)
                .build();
        loan = loanRepo.save(loan);
        history(loan, null, LoanStatus.APPLIED, "SYSTEM");
        return loan;
    }

    @Override
    @Transactional
    public List<UUID> pickAppliedLoansBatch(int limit) {
        List<Object> rows = loanRepo.pickAppliedIds(limit);
        List<UUID> ids = new ArrayList<>();
        for (Object o : rows) {
            if (o instanceof UUID) ids.add((UUID)o);
            else if (o instanceof java.util.Map) {
                Object v = ((java.util.Map<?,?>) o).values().iterator().next();
                ids.add((UUID) v);
            } else {
                // Try generic cast
                try { ids.add((UUID) o); } catch (Exception ignored) {}
            }
        }
        log.debug("Picked {} loans for processing", ids.size());
        return ids;
    }

    @Override
    @Transactional
    public void systemProcess(UUID loanId) {
        Loan loan = loanRepo.findByIdForUpdate(loanId)
                .orElseThrow(() -> new NoSuchElementException("loan not found"));

        // simulate delay 10-25s
        try {
            long delay = ThreadLocalRandom.current().nextLong(10_000L, 25_000L);
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}

        if (loan.getApplicationStatus() != LoanStatus.APPLIED) {
            return; // idempotent
        }

        LoanStatus next = decide(loan);
        transition(loan, next, "SYSTEM");

        if (next == LoanStatus.UNDER_REVIEW) {
            Agent agent = assignAvailableAgent();
            loan.setAssignedAgent(agent);
            loan.setUpdatedAt(OffsetDateTime.now());
            em.merge(loan);
            notifications.pushToAgent(agent, "New loan " + loan.getLoanPublicId());
            notifications.pushToManager(agent.getManager(), "Agent "+agent.getName()+" assigned loan " + loan.getLoanPublicId());
        } else if (next == LoanStatus.APPROVED_BY_SYSTEM) {
            notifications.smsToCustomer(loan.getCustomer(), "Your loan "+loan.getLoanPublicId()+" is approved by system.");
        } else if (next == LoanStatus.REJECTED_BY_SYSTEM) {
            notifications.smsToCustomer(loan.getCustomer(), "Your loan "+loan.getLoanPublicId()+" is rejected by system.");
        }
    }

    private LoanStatus decide(Loan loan) {
        // Simple rules: approve small amounts (<= 200,000), reject if phone invalid, else UNDER_REVIEW
        String phone = loan.getCustomer().getPhone();
        if (phone == null || !phone.matches("[0-9+\\-() ]{8,15}")) {
            return LoanStatus.REJECTED_BY_SYSTEM;
        }
        if (loan.getLoanAmount() <= 200000.0 && loan.getApplicationStatus() == LoanStatus.APPLIED) {
            return LoanStatus.APPROVED_BY_SYSTEM;
        }
        return LoanStatus.UNDER_REVIEW;
    }

    @Override
    @Transactional
    public Loan agentDecision(UUID agentId, String loanPublicId, String decision) {
        Loan loan = loanRepo.findByLoanPublicId(loanPublicId)
                .orElseThrow(() -> new NoSuchElementException("loan not found"));
        if (loan.getApplicationStatus() != LoanStatus.UNDER_REVIEW)
            throw new IllegalStateException("loan is not under review");
        if (loan.getAssignedAgent() == null || !loan.getAssignedAgent().getId().equals(agentId))
            throw new IllegalStateException("loan not assigned to this agent");

        LoanStatus next = "APPROVE".equalsIgnoreCase(decision) ?
                LoanStatus.APPROVED_BY_AGENT : LoanStatus.REJECTED_BY_AGENT;
        transition(loan, next, agentId.toString());
        if (next == LoanStatus.APPROVED_BY_AGENT) {
            notifications.smsToCustomer(loan.getCustomer(), "Your loan "+loan.getLoanPublicId()+" is approved by agent.");
        }
        return loan;
    }

    @Override
    public java.util.Map<LoanStatus, Long> statusCounts() {
        java.util.Map<LoanStatus, Long> map = new java.util.EnumMap<>(LoanStatus.class);
        for (Object[] row : loanRepo.countByStatus()) {
            LoanStatus st = (LoanStatus) row[0];
            Number n = (Number) row[1];
            map.put(st, n.longValue());
        }
        return map;
    }

    @Override
    public Page<Loan> byStatus(LoanStatus status, Pageable pageable) {
        return loanRepo.findByApplicationStatus(status, pageable);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> topCustomers(int limit) {
        String sql = "SELECT c.name, c.phone, COUNT(*) AS approved_count " +
                "FROM loans l JOIN customers c ON l.customer_id=c.id " +
                "WHERE l.application_status IN ('APPROVED_BY_SYSTEM','APPROVED_BY_AGENT') " +
                "GROUP BY c.id, c.name, c.phone " +
                "ORDER BY approved_count DESC " +
                "LIMIT ?";
        return jdbc.query(sql, ps -> ps.setInt(1, limit), (rs, i) -> {
            java.util.Map<String,Object> m = new java.util.LinkedHashMap<>();
            m.put("name", rs.getString("name"));
            m.put("phone", rs.getString("phone"));
            m.put("approved_count", rs.getLong("approved_count"));
            return m;
        });
    }

    @Override
    public Agent assignAvailableAgent() {
        String sql = "SELECT a.id FROM agents a " +
            "LEFT JOIN loans l ON l.assigned_agent_id=a.id AND l.application_status='UNDER_REVIEW' " +
            "WHERE a.is_available = TRUE " +
            "GROUP BY a.id " +
            "ORDER BY COUNT(l.id) ASC, MIN(a.created_at) ASC " +
            "LIMIT 1";
        java.util.List<java.util.UUID> ids = jdbc.query(sql, (rs, i) -> (java.util.UUID) rs.getObject("id"));
        if (ids.isEmpty()) {
            Agent created = agentRepo.save(Agent.builder().name("Default Agent").isAvailable(true).build());
            return created;
        }
        return agentRepo.findById(ids.get(0)).orElseThrow();
    }

    private void history(Loan loan, LoanStatus oldS, LoanStatus newS, String by) {
        LoanStatusHistory h = LoanStatusHistory.builder()
                .loan(loan)
                .oldStatus(oldS)
                .newStatus(newS)
                .changedBy(by)
                .build();
        em.persist(h);
    }

    private void transition(Loan loan, LoanStatus next, String by) {
        LoanStatus old = loan.getApplicationStatus();
        loan.setApplicationStatus(next);
        loan.setUpdatedAt(OffsetDateTime.now());
        em.merge(loan);
        history(loan, old, next, by);
        log.info("Transition {} -> {} for {} by {}", old, next, loan.getLoanPublicId(), by);
    }
}
