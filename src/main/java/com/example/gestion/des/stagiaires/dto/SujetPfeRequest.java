package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.NIVEAU;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class SujetPfeRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La mission est obligatoire")
    private String mission;

    @NotEmpty(message = "Au moins une spécialité universitaire est obligatoire")
    private List<Long> specialiteUniversitaireIds;

    @NotEmpty(message = "Au moins une compétence est obligatoire")
    private List<Long> competenceIds;

    @Min(value = 1, message = "Le nombre de stagiaires doit être au moins 1")
    private int nombreStagiaires;

    @NotNull(message = "Le niveau académique est obligatoire")
    private NIVEAU niveauAcademique;

    @Min(value = 1, message = "La durée en mois doit être au moins 1")
    private int dureeEnMois;

    @NotNull(message = "Le département est obligatoire")
    private UUID departementId;
}
