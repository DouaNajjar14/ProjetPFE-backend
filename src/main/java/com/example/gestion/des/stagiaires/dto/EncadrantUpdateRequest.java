package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.*;
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
public class EncadrantUpdateRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    private String tel;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité maximale doit être au moins 1")
    private Integer capaciteMax;

    @NotNull(message = "Le département est obligatoire")
    private UUID departementId;

    private List<Long> specialiteIds;

    private List<Long> competenceIds;
}
