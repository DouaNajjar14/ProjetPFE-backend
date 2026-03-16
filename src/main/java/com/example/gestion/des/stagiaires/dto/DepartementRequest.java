package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartementRequest {

    @NotBlank(message = "Le nom du département est obligatoire")
    private String nom;

    private String responsable;
}
