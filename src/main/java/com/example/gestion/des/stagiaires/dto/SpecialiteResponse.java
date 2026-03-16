package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialiteResponse {
    private Long id;
    private String nom;
    private UUID departementId;
    private String departementNom;
    private List<CompetenceResponse> competences;
}
