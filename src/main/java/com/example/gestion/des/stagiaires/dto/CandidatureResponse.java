package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CandidatureResponse — DTO pour les réponses API concernant les candidatures
 * Contient toutes les informations de la candidature pour les clients
 * (web/mobile)
 */
@Data
@Builder
public class CandidatureResponse {
    // ─ Identité et type
    private UUID id;
    private TypeStage typeStage;

    // ─ Session (calculée)
    private SessionType session;
    private Integer annee;
    private String anneeAcademique;

    // ─ Statut et flags
    private StatutCandidature statut;
    private Boolean estBinome;
    private Boolean avertissementDoublonAccepte;

    // ─ Dates principales
    private LocalDateTime dateDepot;
    private LocalDateTime dateEntretien;
    private LocalDateTime dateTraitement;
    private LocalDate dateDebut;

    // ─ Phase 2
    private StatutDocuments statutDocuments;
    private Boolean conventionValidee;
    private StatutGlobal statutGlobal;
    private LocalDateTime dateAffectation;

    // ─ Candidats
    private CandidatResponse candidat1;
    private CandidatResponse candidat2;

    // ─ Sujets PFE
    private SujetPfeSimpleResponse sujetChoix1;
    private SujetPfeSimpleResponse sujetChoix2;

    // ─ Relations d'affectation (Phase 3)
    private UUID departementId;
    private String departementNom;
    private UUID encadrantId;
    private String encadrantNom;
}
