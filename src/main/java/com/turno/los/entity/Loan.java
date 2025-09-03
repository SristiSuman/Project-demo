package com.turno.los.entity;

import com.turno.los.entity.enums.LoanStatus;
import com.turno.los.entity.enums.LoanType;
import javax.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="loans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Loan {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable=false, unique = true)
    private String loanPublicId;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="customer_id")
    private Customer customer;

    @Column(nullable=false)
    private Double loanAmount;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private LoanType loanType;

    @Builder.Default
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private LoanStatus applicationStatus = LoanStatus.APPLIED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="assigned_agent_id")
    private Agent assignedAgent;

    @Builder.Default
    @Column(nullable=false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    @Column(nullable=false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
