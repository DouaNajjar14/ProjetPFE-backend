package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifierCoordonneeRequest {

    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "Veuillez fournir une adresse email valide")
    private String email;

    @NotBlank(message = "Le téléphone ne peut pas être vide")
    private String telephone;

    // NOTE: nom et prenom sont INTENTIONNELLEMENT ABSENTS
    // Le backend ne doit JAMAIS modifier ces champs
    // Voir ProfilService pour la logique de validation
}
