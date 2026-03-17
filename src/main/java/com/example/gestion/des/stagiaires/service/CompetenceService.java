package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.CompetenceRequest;
import com.example.gestion.des.stagiaires.dto.CompetenceResponse;
import com.example.gestion.des.stagiaires.entity.Competence;
import com.example.gestion.des.stagiaires.entity.Specialite;
import com.example.gestion.des.stagiaires.repository.CompetenceRepository;
import com.example.gestion.des.stagiaires.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final SpecialiteRepository specialiteRepository;

    public CompetenceResponse creer(CompetenceRequest request) {
        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom de la compétence est obligatoire");
        }

        Specialite specialite = null;
        if (request.getSpecialiteId() != null) {
            specialite = specialiteRepository.findById(request.getSpecialiteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Spécialité non trouvée avec l'id : " + request.getSpecialiteId()));

            if (Boolean.TRUE.equals(specialite.getArchive())) {
                throw new RuntimeException("Impossible d'ajouter une compétence à une spécialité archivée");
            }

            // Check if competence name already exists in this specialite
            if (competenceRepository.existsByNomAndSpecialiteIdAndArchiveFalse(nomNormalise,
                    request.getSpecialiteId())) {
                throw new RuntimeException("Une compétence avec ce nom existe déjà dans cette spécialité");
            }
        }

        Competence competence = Competence.builder()
                .nom(nomNormalise)
                .specialite(specialite)
                .archive(false)
                .build();

        Competence saved = competenceRepository.save(competence);
        return toResponse(saved);
    }

    public CompetenceResponse modifier(Long id, CompetenceRequest request) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));

        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom de la compétence est obligatoire");
        }

        Specialite specialite = null;
        if (request.getSpecialiteId() != null) {
            specialite = specialiteRepository.findById(request.getSpecialiteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Spécialité non trouvée avec l'id : " + request.getSpecialiteId()));

            if (Boolean.TRUE.equals(specialite.getArchive())) {
                throw new RuntimeException("Impossible de lier une compétence à une spécialité archivée");
            }

            // Check if competence name already exists in this specialite (excluding current
            // one)
            if (competenceRepository.existsByNomAndSpecialiteIdAndArchiveFalseAndIdNot(nomNormalise,
                    request.getSpecialiteId(), id)) {
                throw new RuntimeException("Une compétence avec ce nom existe déjà dans cette spécialité");
            }
        }

        competence.setNom(nomNormalise);
        competence.setSpecialite(specialite);
        Competence updated = competenceRepository.save(competence);
        return toResponse(updated);
    }

    public void supprimer(Long id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));

        if (Boolean.TRUE.equals(competence.getArchive())) {
            return;
        }

        competence.setArchive(true);
        competenceRepository.save(competence);
    }

    @Transactional
    public CompetenceResponse desarchiver(Long id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));

        if (!Boolean.TRUE.equals(competence.getArchive())) {
            return toResponse(competence);
        }

        if (competence.getSpecialite() != null && Boolean.TRUE.equals(competence.getSpecialite().getArchive())) {
            throw new RuntimeException("Impossible de désarchiver une compétence d'une spécialité archivée");
        }

        competence.setArchive(false);
        Competence restored = competenceRepository.save(competence);
        return toResponse(restored);
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> listerToutes() {
        return competenceRepository.findByArchiveFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> listerParSpecialite(Long specialiteId) {
        return competenceRepository.findBySpecialiteIdAndArchiveFalse(specialiteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> listerArchivesParSpecialite(Long specialiteId) {
        return competenceRepository.findBySpecialiteIdAndArchiveTrue(specialiteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompetenceResponse trouverParId(Long id) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));
        return toResponse(competence);
    }

    public CompetenceResponse toResponse(Competence competence) {
        return CompetenceResponse.builder()
                .id(competence.getId())
                .nom(competence.getNom())
                .specialiteId(competence.getSpecialite() != null ? competence.getSpecialite().getId() : null)
                .specialiteNom(competence.getSpecialite() != null ? competence.getSpecialite().getNom() : null)
                .build();
    }
}
