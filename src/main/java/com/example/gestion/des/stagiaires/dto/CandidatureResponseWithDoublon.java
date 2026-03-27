package com.example.gestion.des.stagiaires.dto;

import lombok.Builder;
import lombok.Data;

/**
 * CandidatureResponseWithDoublon — Réponse API complète incluant infos de
 * doublon
 * Utile lors de la création si un doublon Niveau 2 est détecté
 */
@Data
@Builder
public class CandidatureResponseWithDoublon {
    /**
     * La candidature créée (ou en attente de confirmation)
     */
    private CandidatureResponse candidature;

    /**
     * Infos sur le doublon détecté (si applicable)
     */
    private DoublonCheckResponse doublonCheck;

    /**
     * true si la candidature a été créée malgré l'avertissement
     */
    private Boolean success;

    /**
     * Message d'état global
     */
    private String message;
}
