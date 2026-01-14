package com.insurance.claimsprocessing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_audit_trail")
@Data
@NoArgsConstructor
public class ClaimAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    private String claimNumber;

    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;


    private String itGuyName; // Stores who performed the action

    @CreationTimestamp
    private LocalDateTime changeDate;
}