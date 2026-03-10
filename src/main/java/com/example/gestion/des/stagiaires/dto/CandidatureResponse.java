package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CandidatureResponse {
    private UUID id;
    private TypeStage typeStage;
    private StatutCandidature statut;
    private Boolean estBinome;
    private LocalDateTime dateDepot;
    private LocalDateTime dateEntretien;

    // Candidat principal
    private CandidatResponse candidat1;

    // Candidat binôme (optionnel)
    private CandidatResponse candidat2;

    // Sujets PFE
    private SujetPfeSimpleResponse sujetChoix1;
    private SujetPfeSimpleResponse sujetChoix2;
}
