package com.turno.los.repository;

import com.turno.los.entity.Customer;
import com.turno.los.entity.Loan;
import com.turno.los.entity.enums.LoanStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, String> {
    Optional<Loan> findByIdForUpdate(UUID loanId);
    List<Loan> findByStatus(LoanStatus status);
    List<Object> pickAppliedIds(int limit);
    Optional<Customer> findCustomerByIdForUpdate(UUID loanId);
    Object[][] countByStatus();
    Page<Loan> findByApplicationStatus(LoanStatus status, Pageable pageable);
    Optional<Loan> findByLoanPublicId(String loanPublicId);
}

