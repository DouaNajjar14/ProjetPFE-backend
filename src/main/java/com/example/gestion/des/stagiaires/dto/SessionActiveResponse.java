package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionActiveResponse {
    private String id;
    private String appareil; // "Windows", "MacOS", "Linux", "iPhone", "Android"
    private String navigateur; // "Chrome", "Firefox", "Safari", "Edge"
    private String ville; // Ville déduite de l'IP
    private String etat; // "Actif", "Inactif"
    private String dateCreation; // Date de création de la session
    private String dernierAcces; // Dernier accès
    private Boolean sessionActuelle; // true si c'est la session courante
}
