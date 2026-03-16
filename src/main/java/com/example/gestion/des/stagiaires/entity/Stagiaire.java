package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.StatutStagiaire;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stagiaires")
public class Stagiaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Compte de connexion créé lors du confirm.
     * La demande initiale parlait d'un FK user/users ;
     * dans ce projet le compte est stocké dans la table `utilisateurs`.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Utilisateur user;

    /**
     * Candidat source de la candidature.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidature_id", nullable = false, unique = true)
    private Candidature candidature;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    /**
     * Université liée au candidat.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universite_id")
    private Universite universite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeStage typeStage;

    /**
     * Département d'affectation du stage.
     * Nullable : il pourra être affecté plus tard par le RH.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "departement_id", nullable = true)
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encadrant_id")
    private Encadrant encadrant;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutStagiaire statut = StatutStagiaire.ACTIF;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
