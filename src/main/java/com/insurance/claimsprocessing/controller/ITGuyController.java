package com.insurance.claimsprocessing.controller;

import com.insurance.claimsprocessing.entity.ITGuy;
import com.insurance.claimsprocessing.enums.ITGuyStatus;
import com.insurance.claimsprocessing.repository.ITGuyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/it-guys")
public class ITGuyController {

    private final ITGuyRepository itGuyRepository;

    public ITGuyController(ITGuyRepository itGuyRepository) {
        this.itGuyRepository = itGuyRepository;
    }

    // 1. GET ALL IT GUYS (See who is Busy/Available)
    @GetMapping
    public ResponseEntity<List<ITGuy>> getAllITGuys() {
        return ResponseEntity.ok(itGuyRepository.findAll());
    }

    // 2. HIRE NEW IT GUY (Add via Postman)
    @PostMapping
    public ResponseEntity<ITGuy> hireITGuy(@RequestBody ITGuy itGuy) {
        // Default to AVAILABLE if they forget to set it
        if (itGuy.getStatus() == null) {
            itGuy.setStatus(ITGuyStatus.AVAILABLE);
        }
        return ResponseEntity.ok(itGuyRepository.save(itGuy));
    }

    // 3. (BONUS) RESET BUTTON: Make everyone AVAILABLE again
    // Useful if you want to re-run your demo from scratch!
    @PutMapping("/reset")
    public ResponseEntity<String> resetWorkforce() {
        List<ITGuy> allGuys = itGuyRepository.findAll();
        for (ITGuy guy : allGuys) {
            guy.setStatus(ITGuyStatus.AVAILABLE);
        }
        itGuyRepository.saveAll(allGuys);
        return ResponseEntity.ok("All IT Guys are now marked as AVAILABLE.");
    }
    // 5. FIRE IT GUY (Delete by ID)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> fireITGuy(@PathVariable Long id) {
        ITGuy guy = itGuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IT Guy not found"));

        // SAFETY CHECK: Don't fire them if they are in the middle of a job!
        if (guy.getStatus() == ITGuyStatus.BUSY) {
            throw new RuntimeException("Cannot remove " + guy.getName() + " while they are BUSY with a claim.");
        }

        itGuyRepository.delete(guy);
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}