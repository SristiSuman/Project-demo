package com.turno.los.entity;

import javax.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="agents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Agent {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable=false)
    private String name;

    @Column(unique = true)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id")
    private Agent manager;

    @Column(nullable=false)
    @Builder.Default
    private boolean isAvailable = true;

    @Column(nullable=false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable=false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
