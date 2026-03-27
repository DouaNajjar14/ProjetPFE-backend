package com.example.gestion.des.stagiaires.service.doublon;

import lombok.Builder;
import lombok.Data;

/**
 * DoublonCheckResult — Résultat de la vérification des doublons
 * Contient le type de doublon (Niveau 1, 2 ou 3) et des messages descriptifs
 */
@Data
@Builder
public class DoublonCheckResult {

    /**
     * Type de doublon : AUCUN | NIVEAU_1 | NIVEAU_2 | NIVEAU_3
     */
    private DoublonLevel niveau;

    /**
     * Message descriptif du problème détecté
     * Utilisé pour les logs et les retours vers le frontend
     */
    private String message;

    /**
     * Détails supplémentaires (ID candature existante, email, etc.)
     */
    private String details;

    /**
     * true si le doublon doit BLOQUER la soumission (Niveau 1)
     * false si c'est un avertissement ou une info (Niveau 2, 3)
     */
    private Boolean isBlocking;

    /**
     * Description lisible pour l'interface RH
     */
    private String userMessage;

    // Utilitaires

    public static DoublonCheckResult aucun() {
        return DoublonCheckResult.builder()
                .niveau(DoublonLevel.AUCUN)
                .isBlocking(false)
                .message("Aucun doublon détecté")
                .build();
    }

    public static DoublonCheckResult niveau1(String message, String details, String userMessage) {
        return DoublonCheckResult.builder()
                .niveau(DoublonLevel.NIVEAU_1)
                .isBlocking(true)
                .message(message)
                .details(details)
                .userMessage(userMessage)
                .build();
    }

    public static DoublonCheckResult niveau2(String message, String details, String userMessage) {
        return DoublonCheckResult.builder()
                .niveau(DoublonLevel.NIVEAU_2)
                .isBlocking(false)
                .message(message)
                .details(details)
                .userMessage(userMessage)
                .build();
    }

    public static DoublonCheckResult niveau3(String message, String details, String userMessage) {
        return DoublonCheckResult.builder()
                .niveau(DoublonLevel.NIVEAU_3)
                .isBlocking(false)
                .message(message)
                .details(details)
                .userMessage(userMessage)
                .build();
    }
}
