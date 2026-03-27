package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CandidatureUpdateRequest {
    @NotNull(message = "Le statut est obligatoire")
    private StatutCandidature statut;

    private LocalDateTime dateEntretien;

    // Phase 1 — Dates du stage après acceptation
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
