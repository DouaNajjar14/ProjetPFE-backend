package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.CompetenceRequest;
import com.example.gestion.des.stagiaires.dto.CompetenceResponse;
import com.example.gestion.des.stagiaires.entity.Competence;
import com.example.gestion.des.stagiaires.entity.Specialite;
import com.example.gestion.des.stagiaires.repository.CompetenceRepository;
import com.example.gestion.des.stagiaires.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final SpecialiteRepository specialiteRepository;

    public CompetenceResponse creer(CompetenceRequest request) {
        Specialite specialite = null;
        if (request.getSpecialiteId() != null) {
            specialite = specialiteRepository.findById(request.getSpecialiteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Spécialité non trouvée avec l'id : " + request.getSpecialiteId()));

            // Check if competence name already exists in this specialite
            if (competenceRepository.existsByNomAndSpecialiteId(request.getNom(), request.getSpecialiteId())) {
                throw new RuntimeException("Une compétence avec ce nom existe déjà dans cette spécialité");
            }
        }

        Competence competence = Competence.builder()
                .nom(request.getNom())
                .specialite(specialite)
                .build();

        Competence saved = competenceRepository.save(competence);
        return toResponse(saved);
    }

    public CompetenceResponse modifier(Long id, CompetenceRequest request) {
        Competence competence = competenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compétence non trouvée avec l'id : " + id));

        Specialite specialite = null;
        if (request.getSpecialiteId() != null) {
            specialite = specialiteRepository.findById(request.getSpecialiteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Spécialité non trouvée avec l'id : " + request.getSpecialiteId()));

            // Check if competence name already exists in this specialite (excluding current
            // one)
            if (!competence.getNom().equals(request.getNom())
                    && competenceRepository.existsByNomAndSpecialiteId(request.getNom(), request.getSpecialiteId())) {
                throw new RuntimeException("Une compétence avec ce nom existe déjà dans cette spécialité");
            }
        }

        competence.setNom(request.getNom());
        competence.setSpecialite(specialite);
        Competence updated = competenceRepository.save(competence);
        return toResponse(updated);
    }

    public void supprimer(Long id) {
        if (!competenceRepository.existsById(id)) {
            throw new RuntimeException("Compétence non trouvée avec l'id : " + id);
        }
        competenceRepository.deleteById(id);
    }

    public List<CompetenceResponse> listerToutes() {
        return competenceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CompetenceResponse> listerParSpecialite(Long specialiteId) {
        return competenceRepository.findBySpecialiteId(specialiteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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
