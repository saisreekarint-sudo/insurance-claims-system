package com.insurance.claimsprocessing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimRequest {

    @NotBlank(message = "Policy Number is required")
    private String policyNumber;

    @NotNull(message = "Claim Type is required")
    private String claimType; // Note: You might need to change this to String if it was Enum in DTO

    @NotNull(message = "Claim Amount is required")
    @Min(value = 0, message = "Claim Amount cannot be negative") // <-- This fixes your negative number issue
    private Double claimAmount;

    @NotBlank(message = "Description is required")
    private String description;
}
//{
//    "policyNumber": "POL-12345",
//    "claimType": "THEFT",
//    "claimAmount": 80000.0,
//    "description": "Entire car stolen"
//}

//CLM-eccf4bba