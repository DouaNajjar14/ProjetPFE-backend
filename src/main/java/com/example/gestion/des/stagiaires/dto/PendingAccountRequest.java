package com.example.gestion.des.stagiaires.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAccountRequest {

    private UUID candidatureId;
    private String prenom;
    private String nom;
    private String email;
    private String username;
    private String tempPassword;
    private String departement;
    private UUID encadrantId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String typeStage;
}

