package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncadrantResponse {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private Integer capaciteMax;
    private Integer capaciteActuelle;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private UUID departementId;
    private String departementNom;
    private List<SpecialiteResponse> specialites;
    private List<CompetenceResponse> competences;
}
