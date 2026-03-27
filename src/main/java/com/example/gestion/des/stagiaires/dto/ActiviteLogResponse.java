package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteLogResponse {
    private String id;
    private String type; // "CONNEXION", "MODIFICATION_PROFIL", "CHANGEMENT_MDP", etc.
    private String description;
    private String dateTime;
    private String adresse_ip;
    private String navigateur;
}
