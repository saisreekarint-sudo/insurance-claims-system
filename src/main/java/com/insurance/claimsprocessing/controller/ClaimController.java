package com.insurance.claimsprocessing.controller;

import com.insurance.claimsprocessing.dto.ClaimRequest;
import com.insurance.claimsprocessing.entity.Claim;
import com.insurance.claimsprocessing.entity.ClaimAudit;
import com.insurance.claimsprocessing.enums.ClaimStatus;
import com.insurance.claimsprocessing.repository.ClaimAuditRepository;
import com.insurance.claimsprocessing.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimAuditRepository auditRepository; // New injection

    public ClaimController(ClaimService claimService, ClaimAuditRepository auditRepository) {
        this.claimService = claimService;
        this.auditRepository = auditRepository;
    }

    // Endpoint: POST /api/claims
    //Register a claim
    @PostMapping
    public ResponseEntity<Claim> registerClaim(@Valid @RequestBody ClaimRequest request) {
        Claim newClaim = claimService.registerClaim(request);
        return ResponseEntity.ok(newClaim);
    }

    // 2. UPDATE STATUS (Requires IT Guy Name)
    @PatchMapping("/{claimNumber}/status")
    public ResponseEntity<Claim> updateStatus(
            @PathVariable String claimNumber,
            @RequestParam com.insurance.claimsprocessing.enums.ClaimStatus status,
            @RequestParam String itGuyName) {

        Claim updatedClaim = claimService.updateClaimStatus(claimNumber, status, itGuyName);
        return ResponseEntity.ok(updatedClaim);
    }

    // 3. SETTLE CLAIM, Requires IT Guy Name && there are actually 2 endpoints
    // and the settle amount is actually optional to negotiate
    @PutMapping("/{claimNumber}/settle")
    public ResponseEntity<Claim> settleClaim(
            @PathVariable String claimNumber,
            @RequestParam(required = false) Double amount, // <--- CHANGED: Added (required = false)
            @RequestParam String requestorName) {

        Claim settledClaim = claimService.settleClaim(claimNumber, amount, requestorName);
        return ResponseEntity.ok(settledClaim);
    }
    //to get the audit of a particular claim by it's ID
    @GetMapping("/{claimNumber}/audit")
    public ResponseEntity<List<ClaimAudit>> getAuditLogs(@PathVariable String claimNumber) {
        List<ClaimAudit> logs = auditRepository.findByClaimNumber(claimNumber);
        return ResponseEntity.ok(logs);
    }
    //to get the whole audit table
    @GetMapping("/audit")
    public ResponseEntity<List<ClaimAudit>> getAllAudits() {
        return ResponseEntity.ok(auditRepository.findAll());
    }
}