package com.insurance.claimsprocessing.config;

import com.insurance.claimsprocessing.entity.ITGuy;
import com.insurance.claimsprocessing.entity.Policy;
import com.insurance.claimsprocessing.enums.ITGuyStatus;
import com.insurance.claimsprocessing.enums.PolicyStatus;
import com.insurance.claimsprocessing.repository.ITGuyRepository;
import com.insurance.claimsprocessing.repository.PolicyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PolicyRepository policyRepository;
    private final ITGuyRepository itGuyRepository; // 1. Declared here

    // 2. Injected BOTH in the constructor
    public DataSeeder(PolicyRepository policyRepository, ITGuyRepository itGuyRepository) {
        this.policyRepository = policyRepository;
        this.itGuyRepository = itGuyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedPolicies();
        seedITGuys();
    }

    private void seedPolicies() {
        // Prevent duplicates on restart
        if (policyRepository.count() > 0) {
            return;
        }

        // 1. Standard Active Policy
        Policy p1 = new Policy();
        p1.setPolicyNumber("POL-12345");
        p1.setSumInsured(100000.0);
        p1.setStatus(PolicyStatus.ACTIVE);
        p1.setStartDate(LocalDate.now().minusMonths(6));
        p1.setEndDate(LocalDate.now().plusMonths(6));

        // 2. High Value Policy
        Policy p2 = new Policy();
        p2.setPolicyNumber("POL-99999");
        p2.setSumInsured(500000.0);
        p2.setStatus(PolicyStatus.ACTIVE);
        p2.setStartDate(LocalDate.now().minusMonths(1));
        p2.setEndDate(LocalDate.now().plusYears(1));

        // 3. Expired Policy
        Policy p3 = new Policy();
        p3.setPolicyNumber("POL-00000");
        p3.setSumInsured(10000.0);
        p3.setStatus(PolicyStatus.EXPIRED);
        p3.setStartDate(LocalDate.now().minusYears(2));
        p3.setEndDate(LocalDate.now().minusDays(1));

        // 4. Future Policy
        Policy p4 = new Policy();
        p4.setPolicyNumber("POL-FUTURE");
        p4.setSumInsured(100000.0);
        p4.setStatus(PolicyStatus.ACTIVE);
        p4.setStartDate(LocalDate.now().plusDays(5));
        p4.setEndDate(LocalDate.now().plusYears(1));

        // 5. Micro Policy
        Policy p5 = new Policy();
        p5.setPolicyNumber("POL-MICRO");
        p5.setSumInsured(2000.0);
        p5.setStatus(PolicyStatus.ACTIVE);
        p5.setStartDate(LocalDate.now().minusMonths(1));
        p5.setEndDate(LocalDate.now().plusYears(1));

        // 6. Terminated Policy
        Policy p6 = new Policy();
        p6.setPolicyNumber("POL-CANCELLED");
        p6.setSumInsured(50000.0);
        p6.setStatus(PolicyStatus.TERMINATED);
        p6.setStartDate(LocalDate.now().minusMonths(6));
        p6.setEndDate(LocalDate.now().plusMonths(6));

        policyRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
        System.out.println("Dummy Policies added");
    }

    private void seedITGuys() {
        if (itGuyRepository.count() == 0) {
            ITGuy g1 = new ITGuy();
            g1.setName("Alice (Senior)");
            g1.setEmail("alice@company.com");
            g1.setStatus(ITGuyStatus.AVAILABLE);

            ITGuy g2 = new ITGuy();
            g2.setName("Bob (Junior)");
            g2.setEmail("bob@company.com");
            g2.setStatus(ITGuyStatus.AVAILABLE);

            itGuyRepository.saveAll(Arrays.asList(g1, g2));
            System.out.println("IT Guys hired.");
        }
    }
}