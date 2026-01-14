package com.insurance.claimsprocessing.repository;

import com.insurance.claimsprocessing.entity.ClaimAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimAuditRepository extends JpaRepository<ClaimAudit, Long> {
    // Finds all audit logs for a specific claim number
    List<ClaimAudit> findByClaimNumber(String claimNumber);
}