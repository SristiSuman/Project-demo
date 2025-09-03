package com.turno.los.controller;

import com.turno.los.dto.AgentDecisionRequest;
import com.turno.los.dto.SubmitLoanRequest;
import com.turno.los.entity.Loan;
import com.turno.los.entity.enums.LoanStatus;
import com.turno.los.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/loans")
    public ResponseEntity<Loan> submit(@Valid @RequestBody SubmitLoanRequest req) {
        return ResponseEntity.ok(loanService.submit(req));
    }

    @PutMapping("/agents/{agentId}/loans/{loanPublicId}/decision")
    public ResponseEntity<Loan> agentDecision(@PathVariable UUID agentId,
                                              @PathVariable String loanPublicId,
                                              @Valid @RequestBody AgentDecisionRequest req) {
        return ResponseEntity.ok(loanService.agentDecision(agentId, loanPublicId, req.decision));
    }

    @GetMapping("/loans/status-count")
    public ResponseEntity<Map<LoanStatus, Long>> statusCounts() {
        return ResponseEntity.ok(loanService.statusCounts());
    }

    @GetMapping("/customers/top")
    public ResponseEntity<List<Map<String, Object>>> topCustomers(@RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(loanService.topCustomers(limit));
    }

    @GetMapping("/loans")
    public ResponseEntity<Page<Loan>> byStatus(@RequestParam LoanStatus status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(loanService.byStatus(status, PageRequest.of(page, size)));
    }
}
