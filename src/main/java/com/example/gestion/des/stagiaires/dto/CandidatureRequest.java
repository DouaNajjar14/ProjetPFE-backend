package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.TypeStage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CandidatureRequest {
    @NotNull(message = "Le type de stage est obligatoire")
    private TypeStage typeStage;

    private Boolean estBinome = false;

    // Informations candidat principal
    @NotNull(message = "Les informations du candidat sont obligatoires")
    private CandidatRequest candidat1;

    // Informations candidat binôme (optionnel)
    private CandidatRequest candidat2;

    // Sujets PFE (obligatoires si typeStage = PFE)
    private UUID sujetChoix1Id;
    private UUID sujetChoix2Id;
}
