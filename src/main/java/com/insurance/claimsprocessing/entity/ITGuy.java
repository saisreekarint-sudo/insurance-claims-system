package com.insurance.claimsprocessing.entity;

import com.insurance.claimsprocessing.enums.ITGuyStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "it_guys")
@Data
@NoArgsConstructor
public class ITGuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true, nullable = false)
    private String email;


    @Enumerated(EnumType.STRING)
    private ITGuyStatus status; // AVAILABLE or BUSY

    private String currentClaimNumber;
}