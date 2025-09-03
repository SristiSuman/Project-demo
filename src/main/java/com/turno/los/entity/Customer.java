package com.turno.los.entity;

import javax.persistence.*;

import com.turno.los.entity.enums.LoanStatus;

import lombok.*;
import java.util.UUID;

@Entity @Table(name="customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue
    private UUID id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, unique=true)
    private String phone;

    public LoanStatus getApplicationStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getApplicationStatus'");
    }
}
