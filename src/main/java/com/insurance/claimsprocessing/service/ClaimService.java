package com.insurance.claimsprocessing.service;

import com.insurance.claimsprocessing.dto.ClaimRequest;
import com.insurance.claimsprocessing.entity.Claim;
import com.insurance.claimsprocessing.entity.ClaimAudit;
import com.insurance.claimsprocessing.entity.ITGuy;
import com.insurance.claimsprocessing.entity.Policy;
import com.insurance.claimsprocessing.enums.ClaimStatus;
import com.insurance.claimsprocessing.enums.ClaimType;
import com.insurance.claimsprocessing.enums.ITGuyStatus;
import com.insurance.claimsprocessing.exception.BusinessValidationException;
import com.insurance.claimsprocessing.exception.ResourceNotFoundException;
import com.insurance.claimsprocessing.repository.ClaimAuditRepository;
import com.insurance.claimsprocessing.repository.ClaimRepository;
import com.insurance.claimsprocessing.repository.ITGuyRepository;
import com.insurance.claimsprocessing.repository.PolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimAuditRepository auditRepository;
    private final ITGuyRepository itGuyRepository;

    public ClaimService(ClaimRepository claimRepo, PolicyRepository policyRepo,
                        ClaimAuditRepository auditRepo, ITGuyRepository itGuyRepo) {
        this.claimRepository = claimRepo;
        this.policyRepository = policyRepo;
        this.auditRepository = auditRepo;
        this.itGuyRepository = itGuyRepo;
    }

    // 1. REGISTER CLAIM (Now with AUTO-SCHEDULING)
    public Claim registerClaim(ClaimRequest request) {
        Policy policy = policyRepository.findById(request.getPolicyNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (!policy.isValid()) throw new RuntimeException("Policy invalid or expired");
        if (request.getClaimAmount() > policy.getSumInsured()) throw new BusinessValidationException("Limit exceeded");

        Claim claim = new Claim();
        claim.setPolicy(policy);
        claim.setClaimType(ClaimType.valueOf(request.getClaimType()));
        claim.setClaimAmount(request.getClaimAmount());
        claim.setDescription(request.getDescription());
        claim.setClaimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8));

        // Fraud Check
        double fraudThreshold = policy.getSumInsured() * 0.70;
        if (request.getClaimAmount() > fraudThreshold) {
            claim.setStatus(ClaimStatus.FRAUD_REVIEW);
        } else {
            claim.setStatus(ClaimStatus.REGISTERED);
        }

        // --- SCHEDULING ALGORITHM: First Available (FCFS) ---
        // automatically find a free IT Guy
        ITGuy assignedGuy = itGuyRepository.findFirstByStatus(ITGuyStatus.AVAILABLE)
                .orElse(null);

        if (assignedGuy != null) {
            claim.setAssignedITGuy(assignedGuy);

            // UPDATE STATUS AND CLAIM NUMBER
            assignedGuy.setStatus(ITGuyStatus.BUSY);
            assignedGuy.setCurrentClaimNumber(claim.getClaimNumber()); // <--- ADD THIS
            itGuyRepository.save(assignedGuy);
        }
        // ----------------------------------------------------

        // Save Audit (System action)
        String actor = (assignedGuy != null) ? "SYSTEM (Assigned to " + assignedGuy.getName() + ")" : "SYSTEM";
        saveAudit(claim.getClaimNumber(), null, claim.getStatus().name(), actor,0.0);

        return claimRepository.save(claim);
    }

    // 2. UPDATE STATUS (Checks Ownership)
    public Claim updateClaimStatus(String claimNumber, ClaimStatus newStatus, String requestorName) {
        Claim claim = claimRepository.findAll().stream()
                .filter(c -> c.getClaimNumber().equals(claimNumber))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Claim " + claimNumber + " not found"));

        ITGuy requestor = itGuyRepository.findAll().stream()
                .filter(guy -> guy.getName().equalsIgnoreCase(requestorName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("IT Guy '" + requestorName + "' does not exist."));

        ClaimStatus current = claim.getStatus();

        // OWNERSHIP CHECK
        // If the system assigned this to Alice, Bob cannot touch it.
        if (claim.getAssignedITGuy() != null) {
            if (!claim.getAssignedITGuy().getId().equals(requestor.getId())) {
                throw new BusinessValidationException(
                        "Access Denied. This claim was auto-assigned to " + claim.getAssignedITGuyName() +
                                ", but you are " + requestor.getName());
            }
        } else {
            // EDGE CASE: If no one was available during registration (Backlog),
            // allow the first person who touches it to take ownership.
            if (requestor.getStatus() == ITGuyStatus.BUSY) {
                throw new BusinessValidationException(requestor.getName() + " is already busy!");
            }
            claim.setAssignedITGuy(requestor);
            requestor.setStatus(ITGuyStatus.BUSY);
            itGuyRepository.save(requestor);
        }

        // WORKFLOW RULES
        if (current == ClaimStatus.SETTLED || current == ClaimStatus.REJECTED) {
            throw new RuntimeException("Claim is finalized.");
        }

        // (Simple transition logic)
        boolean isValid = false;
        if (current == ClaimStatus.REGISTERED && (newStatus == ClaimStatus.IN_REVIEW || newStatus == ClaimStatus.REJECTED)) isValid = true;
        else if (current == ClaimStatus.IN_REVIEW && (newStatus == ClaimStatus.APPROVED || newStatus == ClaimStatus.REJECTED)) isValid = true;
        else if (current == ClaimStatus.APPROVED && newStatus == ClaimStatus.SETTLED) isValid = true;
        else if (current == ClaimStatus.FRAUD_REVIEW && (newStatus == ClaimStatus.REJECTED || newStatus == ClaimStatus.IN_REVIEW)) isValid = true;

        if (!isValid) throw new RuntimeException("Invalid Status Transition");

        // RELEASE LOGIC (If Rejected)
        if (newStatus == ClaimStatus.REJECTED && claim.getAssignedITGuy() != null) {
            ITGuy owner = claim.getAssignedITGuy();
            owner.setStatus(ITGuyStatus.AVAILABLE);
            owner.setCurrentClaimNumber(null);
            itGuyRepository.save(owner);
        }

        claim.setStatus(newStatus);
        saveAudit(claimNumber, current.name(), newStatus.name(), requestor.getName(),0.0);

        return claimRepository.save(claim);
    }

    // 3. SETTLE CLAIM
    public Claim settleClaim(String claimNumber, Double settlementAmount, String requestorName) {
        Claim claim = claimRepository.findAll().stream()
                .filter(c -> c.getClaimNumber().equals(claimNumber))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        ITGuy requestor = itGuyRepository.findAll().stream()
                .filter(guy -> guy.getName().equalsIgnoreCase(requestorName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("IT Guy not found"));

        if (claim.getAssignedITGuy() != null && !claim.getAssignedITGuy().getId().equals(requestor.getId())) {
            throw new BusinessValidationException("Access Denied. Only " + claim.getAssignedITGuyName() + " can settle this.");
        }

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED claims can be settled. Current status: " + claim.getStatus());
        }


        Double finalAmount;

        if (settlementAmount == null) {
            // Case 1: No amount provided -> Default to full Claim Amount
            finalAmount = claim.getClaimAmount();
        } else {
            // Case 2: Custom amount provided -> Validate it
            if (settlementAmount > claim.getClaimAmount()) {
                throw new BusinessValidationException("Settlement amount cannot exceed claim amount.");
            }
            finalAmount = settlementAmount;
        }


        ClaimStatus oldStatus = claim.getStatus();

        claim.setSettlementAmount(finalAmount); // Set the calculated amount
        claim.setSettledAt(LocalDateTime.now());
        claim.setStatus(ClaimStatus.SETTLED);

        // RELEASE LOGIC
        if (claim.getAssignedITGuy() != null) {
            ITGuy owner = claim.getAssignedITGuy();
            owner.setStatus(ITGuyStatus.AVAILABLE);
            owner.setCurrentClaimNumber(null);
            itGuyRepository.save(owner);
        }

        saveAudit(claimNumber, oldStatus.name(), ClaimStatus.SETTLED.name(), requestor.getName(),finalAmount);

        return claimRepository.save(claim);
    }

    private void saveAudit(String claimNum, String oldSt, String newSt, String actorName,Double amount) {
        ClaimAudit audit = new ClaimAudit();
        audit.setClaimNumber(claimNum);
        audit.setOldStatus(oldSt);
        audit.setNewStatus(newSt);
        audit.setItGuyName(actorName);
        auditRepository.save(audit);
        audit.setSettlementAmount(amount);
    }
}