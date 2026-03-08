package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import com.example.gestion.des.stagiaires.enums.STATUT;
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
public class SujetPfeResponse {

    private UUID id;
    private String titre;
    private String mission;
    private List<SpecialiteUniversitaireResponse> specialitesUniversitaires;
    private List<CompetenceResponse> competences;
    private int nombreStagiaires;
    private NIVEAU niveauAcademique;
    private STATUT statut;
    private Boolean archive;
    private int dureeEnMois;
    private UUID departementId;
    private String departementNom;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
