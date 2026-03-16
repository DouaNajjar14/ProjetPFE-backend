package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementResponse {

    private UUID id;
    private String nom;
    private String responsable;
    private Integer nombreEncadrantsActuel;
    private Integer nombreStagiairesActuel;
    private Boolean archive;
}
