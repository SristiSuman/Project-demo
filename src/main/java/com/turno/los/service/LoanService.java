package com.turno.los.service;

import com.turno.los.dto.SubmitLoanRequest;
import com.turno.los.entity.Agent;
import com.turno.los.entity.Loan;
import com.turno.los.entity.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LoanService {
    Loan submit(SubmitLoanRequest req);
    List<UUID> pickAppliedLoansBatch(int limit);
    void systemProcess(UUID loanId);
    Loan agentDecision(UUID agentId, String loanPublicId, String decision);
    Map<LoanStatus, Long> statusCounts();
    Page<Loan> byStatus(LoanStatus status, Pageable pageable);
    List<Map<String, Object>> topCustomers(int limit);
    Agent assignAvailableAgent();
    Loan createLoan(Loan loan);
}
