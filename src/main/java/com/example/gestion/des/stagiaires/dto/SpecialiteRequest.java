package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class SpecialiteRequest {
    @NotBlank(message = "Le nom de la spécialité est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;
    
    @NotNull(message = "Le département est obligatoire")
    private UUID departementId;
    
    private List<Long> competenceIds;
}
