package com.insurance.claimsprocessing.controller;

import com.insurance.claimsprocessing.entity.Policy;
import com.insurance.claimsprocessing.exception.ResourceNotFoundException;
import com.insurance.claimsprocessing.repository.PolicyRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyRepository policyRepository;

    public PolicyController(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    // 1. GET ALL POLICIES
    @GetMapping
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }



    // 2. CREATE A NEW POLICY
    @PostMapping
    public Policy createPolicy(@Valid @RequestBody Policy policy) {
        return policyRepository.save(policy);
    }

    // 3. DELETE POLICY (By ID)
    @DeleteMapping("/{policyNumber}")
    public ResponseEntity<Void> deletePolicy(@PathVariable String policyNumber) {
        // 1. Check if it exists
        if (!policyRepository.existsById(policyNumber)) {
            throw new ResourceNotFoundException("Policy " + policyNumber + " not found");
        }
        // 2. Delete it
        policyRepository.deleteById(policyNumber);
        // 3. Return 204 No Content (Standard for successful delete)
        return ResponseEntity.noContent().build();
    }
}