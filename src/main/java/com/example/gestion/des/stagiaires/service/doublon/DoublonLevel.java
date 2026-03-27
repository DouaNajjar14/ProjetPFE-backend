package com.example.gestion.des.stagiaires.service.doublon;

/**
 * DoublonLevel — Énumération des niveaux de détection de doublons
 */
public enum DoublonLevel {
    /**
     * AUCUN : Aucun doublon détecté
     */
    AUCUN("Aucun doublon"),

    /**
     * NIVEAU_1 : Blocage automatique (sans intervention RH)
     * Exemples : même email + même type + même année, Initiation + PFE, etc.
     */
    NIVEAU_1("Bloquer automatiquement"),

    /**
     * NIVEAU_2 : Avertissement RH (popup orange)
     * Exemple : Perfectionnement + PFE même année
     * Le RH peut continuer en cochant l'avertissement
     */
    NIVEAU_2("Avertissement RH"),

    /**
     * NIVEAU_3 : Information discrète (aucun blocage)
     * Exemple : même nom + prénom, email différent (homonyme possible)
     */
    NIVEAU_3("Information discrète");

    private final String label;

    DoublonLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
