package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilUtilisateurResponse {
    private String id;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String role;
    private String statut;
    private String createdAt;
    private String updatedAt;
}
