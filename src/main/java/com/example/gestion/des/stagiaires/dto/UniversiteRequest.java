package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UniversiteRequest {
    @NotBlank(message = "Le nom de l'université est obligatoire")
    private String nom;
}
