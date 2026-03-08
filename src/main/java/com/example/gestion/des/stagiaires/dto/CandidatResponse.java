package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CandidatResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private NIVEAU niveauAcademique;
    private String cv;
    private String lettreMotivation;
    private UUID universiteId;
    private String universiteNom;
}
