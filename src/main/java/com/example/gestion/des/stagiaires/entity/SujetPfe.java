package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import com.example.gestion.des.stagiaires.enums.STATUT;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "sujet_pfe")
public class SujetPfe {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mission;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sujet_pfe_specialites_universitaires", joinColumns = @JoinColumn(name = "sujet_pfe_id"), inverseJoinColumns = @JoinColumn(name = "specialite_universitaire_id"))
    @Builder.Default
    private Set<SpecialiteUniversitaire> specialitesUniversitaires = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sujet_pfe_competences", joinColumns = @JoinColumn(name = "sujet_pfe_id"), inverseJoinColumns = @JoinColumn(name = "competence_id"))
    @Builder.Default
    private Set<Competence> competences = new HashSet<>();

    @Column(nullable = false)
    private int nombreStagiaires;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NIVEAU niveauAcademique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private STATUT statut;

    @Builder.Default
    private Boolean archive = false;

    @Column(nullable = false)
    private int dureeEnMois;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    @Column(updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
