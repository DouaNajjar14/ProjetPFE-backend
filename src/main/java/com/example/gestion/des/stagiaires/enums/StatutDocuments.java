package com.example.gestion.des.stagiaires.enums;

/**
 * StatutDocuments — Énumération pour le statut de validation des documents
 * Phase 2 du workflow RH
 */
public enum StatutDocuments {
    /**
     * NON_DEPOSE : Aucun document n'a été déposé par le candidat
     */
    NON_DEPOSE("Aucun document"),

    /**
     * EN_ATTENTE_VERIF : Documents en attente de vérification par le RH
     */
    EN_ATTENTE_VERIF("En attente de vérification"),

    /**
     * VALIDE : Documents validés par le RH - la convention peut être signée
     */
    VALIDE("Validé"),

    /**
     * REJETE : Documents rejetés par le RH - dépôt à recommencer
     */
    REJETE("Rejeté");

    private final String label;

    StatutDocuments(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
