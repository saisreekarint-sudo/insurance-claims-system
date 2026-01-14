package com.insurance.claimsprocessing.repository;

import com.insurance.claimsprocessing.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {
    // This gives you save(), findById(), findAll() automatically!
}