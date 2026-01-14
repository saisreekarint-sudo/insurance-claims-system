package com.insurance.claimsprocessing.repository;

import com.insurance.claimsprocessing.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    // We can add custom queries here later if needed
}