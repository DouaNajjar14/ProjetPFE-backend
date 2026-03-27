package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceResponse {
    private Long id;
    private String nom;
    private Long specialiteId;
    private String specialiteNom;
    private Boolean archive;
}
