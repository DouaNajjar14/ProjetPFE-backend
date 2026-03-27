package com.example.gestion.des.stagiaires.enums;

/**
 * StatutCandidature — Énumération des statuts de candidature
 * Workflow : EN_ATTENTE → PRESELECTIONNE → ENTRETIEN → ACCEPTE/REFUSE
 */
public enum StatutCandidature {
    /**
     * EN_ATTENTE : Candidature reçue, en attente de traitement RH
     */
    EN_ATTENTE("En attente"),

    /**
     * PRESELECTIONNE : Candidature pré-sélectionnée par le RH
     */
    PRESELECTIONNE("Présélectionné"),

    /**
     * ENTRETIEN : Candidat convoqué pour un entretien
     */
    ENTRETIEN("Entretien"),

    /**
     * ACCEPTE : Candidature acceptée par le RH après entretien
     */
    ACCEPTE("Accepté"),

    /**
     * REFUSE : Candidature refusée par le RH
     */
    REFUSE("Refusé");

    private final String label;

    StatutCandidature(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
