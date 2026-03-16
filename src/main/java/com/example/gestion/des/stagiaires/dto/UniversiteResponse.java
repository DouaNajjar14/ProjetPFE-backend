package com.example.gestion.des.stagiaires.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UniversiteResponse {
    private UUID id;
    private String nom;
}
