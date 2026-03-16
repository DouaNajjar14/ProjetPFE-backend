package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.StatutStagiaire;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagiaireResponse {

    private Long id;

    private UUID userId;
    private UUID candidatId;
    private UUID candidatureId;

    private String prenom;
    private String nom;
    private String email;
    private String telephone;

    private UUID universiteId;
    private String universiteNom;

    private TypeStage typeStage;

    private UUID departementId;
    private String departementNom;

    private UUID encadrantId;
    private String encadrantNomComplet;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private StatutStagiaire statut;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

