package com.example.gestion.des.stagiaires.enums;

/**
 * StatutGlobal — Énumération pour le statut global de la candidature
 * Phase 3 du workflow RH - suivi du stage après affectation
 */
public enum StatutGlobal {
    /**
     * EN_COURS : Le stage est actuellement en cours de réalisation
     */
    EN_COURS("En cours"),

    /**
     * TERMINE : Le stage a été complété avec succès
     */
    TERMINE("Terminé"),

    /**
     * ABANDONNE : Le stage a été abandonné par le stagiaire ou l'encadrant
     */
    ABANDONNE("Abandonné");

    private final String label;

    StatutGlobal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
