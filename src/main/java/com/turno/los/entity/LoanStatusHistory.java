package com.turno.los.entity;

import com.turno.los.entity.enums.LoanStatus;
import javax.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name="loan_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="loan_id")
    private Loan loan;

    @Enumerated(EnumType.STRING)
    private LoanStatus oldStatus;

    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private LoanStatus newStatus;

    @Builder.Default
    @Column(nullable=false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    private String changedBy; // SYSTEM or agent id
}
