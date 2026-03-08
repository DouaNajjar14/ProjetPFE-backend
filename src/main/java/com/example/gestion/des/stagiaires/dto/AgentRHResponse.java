package com.example.gestion.des.stagiaires.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRHResponse {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}

