package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO utilisé pour mettre à jour le statut d'une candidature.
 * - Si le statut est PRESELECTIONNE, dateEntretien est obligatoire
 * (n8n l'utilisera pour créer le lien Google Meet automatiquement).
 * - Si le statut est ACCEPTE, dateDebut est obligatoire
 * (n8n l'inclura dans l'email de confirmation envoyé au candidat).
 */
@Data
public class StatutCandidatureRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutCandidature statut;

    /**
     * Obligatoire si statut = PRESELECTIONNE.
     * Format ISO-8601 : "2026-04-15T10:30:00"
     */
    private LocalDateTime dateEntretien;

    /**
     * Obligatoire si statut = ACCEPTE.
     * Date de début du stage (sans heure).
     * Format ISO-8601 : "2026-05-01"
     */
    private LocalDate dateDebut;

    /**
     * Optionnel si statut = ACCEPTE.
     * Date de fin du stage (sans heure).
     * Format ISO-8601 : "2026-08-31"
     */
    private LocalDate dateFin;
}
