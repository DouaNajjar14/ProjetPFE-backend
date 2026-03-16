package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CandidatRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String tel;

    @NotNull(message = "Le niveau académique est obligatoire")
    private NIVEAU niveauAcademique;

    @NotNull(message = "L'université est obligatoire")
    private UUID universiteId;
}
