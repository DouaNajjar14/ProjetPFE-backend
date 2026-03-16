package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.StagiaireResponse;
import com.example.gestion.des.stagiaires.entity.Stagiaire;
import com.example.gestion.des.stagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StagiaireService {

    private final StagiaireRepository stagiaireRepository;

    @Transactional(readOnly = true)
    public List<StagiaireResponse> listerTous() {
        return stagiaireRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StagiaireResponse trouverParId(Long id) {
        Stagiaire stagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé avec l'id : " + id));
        return toResponse(stagiaire);
    }

    private StagiaireResponse toResponse(Stagiaire stagiaire) {
        return StagiaireResponse.builder()
                .id(stagiaire.getId())
                .userId(stagiaire.getUser() != null ? stagiaire.getUser().getId() : null)
                .candidatId(stagiaire.getCandidat() != null ? stagiaire.getCandidat().getId() : null)
                .candidatureId(stagiaire.getCandidature() != null ? stagiaire.getCandidature().getId() : null)
                .prenom(stagiaire.getPrenom())
                .nom(stagiaire.getNom())
                .email(stagiaire.getEmail())
                .telephone(stagiaire.getTelephone())
                .universiteId(stagiaire.getUniversite() != null ? stagiaire.getUniversite().getId() : null)
                .universiteNom(stagiaire.getUniversite() != null ? stagiaire.getUniversite().getNom() : null)
                .typeStage(stagiaire.getTypeStage())
                .departementId(stagiaire.getDepartement() != null ? stagiaire.getDepartement().getId() : null)
                .departementNom(stagiaire.getDepartement() != null ? stagiaire.getDepartement().getNom() : null)
                .encadrantId(stagiaire.getEncadrant() != null ? stagiaire.getEncadrant().getId() : null)
                .encadrantNomComplet(stagiaire.getEncadrant() != null
                        ? stagiaire.getEncadrant().getPrenom() + " " + stagiaire.getEncadrant().getNom()
                        : null)
                .dateDebut(stagiaire.getDateDebut())
                .dateFin(stagiaire.getDateFin())
                .statut(stagiaire.getStatut())
                .createdAt(stagiaire.getCreatedAt())
                .updatedAt(stagiaire.getUpdatedAt())
                .build();
    }
}

