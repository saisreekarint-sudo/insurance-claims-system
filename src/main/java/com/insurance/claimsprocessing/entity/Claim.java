package com.insurance.claimsprocessing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.claimsprocessing.enums.ClaimStatus;
import com.insurance.claimsprocessing.enums.ClaimType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@NoArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    // "Claim Number (auto-generated)" - We will set this in the Service layer
    @Column(name = "claim_number", unique = true, nullable = false)
    private String claimNumber;

    // Relationship: Many Claims can belong to One Policy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_number", nullable = false)
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    @Column(nullable = false)
    private Double claimAmount;

    @Column(length = 1000) // "Claim Description (min 20 chars)"
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.REGISTERED; // Default status

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "assigned_it_guy_id")
    private ITGuy assignedITGuy;

    // NEW: Helper to show the name in JSON
    public String getAssignedITGuyName() {
        return (assignedITGuy != null) ? assignedITGuy.getName() : "Unassigned";
    }
    // Settlement details (initially null)
    private Double settlementAmount;
    private LocalDateTime settledAt;
}