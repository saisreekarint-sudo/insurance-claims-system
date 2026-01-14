package com.insurance.claimsprocessing.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurance.claimsprocessing.enums.PolicyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
public class Policy {

    @Id
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Policy Number is required")
   //Displaying the policy number because we need to know the ID for deleting a policy
    private String policyNumber;

    @Column(nullable = false)
    @NotNull(message = "Sum Insured is required")
    @Min(value = 1, message = "Sum Insured must be greater than 0")
    private Double sumInsured;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private PolicyStatus status;

    @Column(nullable = false)
    @NotNull(message = "Start Date is required")
    private LocalDate startDate;

    @Column(nullable = false)
    @NotNull(message = "End Date is required")
    @Future(message = "End Date must be in the future")
    private LocalDate endDate;

    // Custom helper to check if policy is currently active
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return status == PolicyStatus.ACTIVE &&
                (now.isEqual(startDate) || now.isAfter(startDate)) &&
                (now.isEqual(endDate) || now.isBefore(endDate));
    }
}