package com.example.gestion.des.stagiaires.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangerMotDePasseRequest {

    @NotBlank(message = "Le mot de passe actuel ne peut pas être vide")
    private String motDePasseActuel;

    @NotBlank(message = "Le nouveau mot de passe ne peut pas être vide")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String nouveauMotDePasse;

    // Validation côté backend:
    // - Le mot de passe actuel doit être vérifié avec passwordEncoder.matches()
    // - Le nouveau mot de passe doit contenir:
    // * Au moins une majuscule
    // * Au moins une minuscule
    // * Au moins un chiffre
    // * Au moins un caractère spécial
    // - Tous les RefreshTokens de cet utilisateur SAUF le token courant doivent
    // être supprimés
}
