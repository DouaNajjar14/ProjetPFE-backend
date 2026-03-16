package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "candidatures")
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeStage typeStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estBinome = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateDepot;

    private LocalDateTime dateEntretien;

    // Date de début du stage — renseignée uniquement lors de l'acceptation
    @Column(nullable = true)
    private LocalDateTime dateDebut;

    // Candidat principal (obligatoire)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat1_id", nullable = false)
    private Candidat candidat1;

    // Candidat binôme (optionnel, uniquement si estBinome = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat2_id")
    private Candidat candidat2;

    // Premier choix de sujet PFE (obligatoire si typeStage = PFE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sujet_choix1_id")
    private SujetPfe sujetChoix1;

    // Deuxième choix de sujet PFE (optionnel)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sujet_choix2_id")
    private SujetPfe sujetChoix2;

    @PrePersist
    protected void onCreate() {
        dateDepot = LocalDateTime.now();
    }
}
