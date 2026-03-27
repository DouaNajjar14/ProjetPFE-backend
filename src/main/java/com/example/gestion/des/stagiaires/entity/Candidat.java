package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "candidats")
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String tel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NIVEAU niveauAcademique;

    @Column(nullable = true)
    private String cv;

    @Column(nullable = true)
    private String lettreMotivation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universite_id", nullable = false)
    private Universite universite;
}
