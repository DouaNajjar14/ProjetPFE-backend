package com.example.gestion.des.stagiaires.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DoublonCheckResponse — Retour des informations de doublon détecté
 * Envoyé au frontend pour afficher des avertissements ou des blocs
 */
@Data
@Builder
public class DoublonCheckResponse {
    /**
     * Niveau du doublon : AUCUN | NIVEAU_1 | NIVEAU_2 | NIVEAU_3
     */
    private String niveau;

    /**
     * true si le doublon doit BLOQUER la soumission (Niveau 1)
     */
    private Boolean isBlocking;

    /**
     * Message technique pour les logs
     */
    private String technicalMessage;

    /**
     * Message lisible pour l'utilisateur dans le frontend
     */
    private String userMessage;

    /**
     * Détails supplémentaires (ID candidature, email, etc.)
     */
    private String details;
}
