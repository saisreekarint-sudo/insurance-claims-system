package com.insurance.claimsprocessing.repository;

import com.insurance.claimsprocessing.entity.ITGuy;
import com.insurance.claimsprocessing.enums.ITGuyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ITGuyRepository extends JpaRepository<ITGuy, Long> {
    // Scheduling Algorithm: Find the first person who is AVAILABLE
    Optional<ITGuy> findFirstByStatus(ITGuyStatus status);
}