package com.example.gestion.des.stagiaires.enums;

/**
 * SessionType — Énumération des sessions de stage
 * Dérivée automatiquement du TypeStage au moment de la création de la
 * candidature
 */
public enum SessionType {
    /**
     * HIVER : Session d'hiver (Initiation + Perfectionnement)
     * Période : 7 janvier → 7 février (fixe chaque année)
     */
    HIVER("Session Hiver"),

    /**
     * PFE : Session PFE
     * Période : 1er février → variable (4-6 mois selon école)
     */
    PFE("Session PFE"),

    /**
     * ETE : Session d'été
     * Période : 1er juillet → 31 août max
     */
    ETE("Session Été");

    private final String label;

    SessionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
