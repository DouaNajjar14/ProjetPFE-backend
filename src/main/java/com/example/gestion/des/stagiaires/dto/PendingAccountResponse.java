package com.example.gestion.des.stagiaires.dto;

import com.example.gestion.des.stagiaires.enums.PendingAccountStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAccountResponse {

    private UUID id;
    private UUID candidatureId;
    private String prenom;
    private String nom;
    private String email;
    private String username;
    private String departement;
    private UUID encadrantId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String typeStage;
    private String token;
    private PendingAccountStatus statut;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    /** Uniquement renseigné lors de la création (POST) — en clair, une seule fois */
    private String tempPassword;

    /**
     * Mot de passe temporaire en clair généré lors du confirm().
     * Renvoyé une seule fois dans la réponse — jamais stocké en base.
     * n8n doit l'utiliser immédiatement pour l'envoyer au stagiaire par email.
     */
    private String tempPasswordClair;

    /** Renseigné après confirmation */
    private UUID stagiaireId;
}

