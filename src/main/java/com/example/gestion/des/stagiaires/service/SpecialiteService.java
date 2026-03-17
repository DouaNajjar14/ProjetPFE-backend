package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.SpecialiteRequest;
import com.example.gestion.des.stagiaires.dto.SpecialiteResponse;
import com.example.gestion.des.stagiaires.entity.Departement;
import com.example.gestion.des.stagiaires.entity.Specialite;
import com.example.gestion.des.stagiaires.repository.CompetenceRepository;
import com.example.gestion.des.stagiaires.repository.DepartementRepository;
import com.example.gestion.des.stagiaires.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialiteService {

    private final SpecialiteRepository specialiteRepository;
    private final DepartementRepository departementRepository;
    private final CompetenceRepository competenceRepository;
    private final CompetenceService competenceService;

    @Transactional
    public SpecialiteResponse creer(SpecialiteRequest request) {
        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom de la spécialité est obligatoire");
        }

        if (specialiteRepository.existsByNomAndDepartementIdAndArchiveFalse(nomNormalise, request.getDepartementId())) {
            throw new RuntimeException("Une spécialité avec ce nom existe déjà dans ce département");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        Specialite specialite = Specialite.builder()
                .nom(nomNormalise)
                .departement(departement)
                .competences(new ArrayList<>())
                .archive(false)
                .build();

        Specialite saved = specialiteRepository.save(specialite);
        return toResponse(saved);
    }

    @Transactional
    public SpecialiteResponse modifier(Long id, SpecialiteRequest request) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));

        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom de la spécialité est obligatoire");
        }

        // Vérifier si le nom est déjà utilisé par une autre spécialité dans le même
        // département
        if (specialiteRepository.existsByNomAndDepartementIdAndArchiveFalseAndIdNot(nomNormalise,
                request.getDepartementId(), id)) {
            throw new RuntimeException("Une spécialité avec ce nom existe déjà dans ce département");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        specialite.setNom(nomNormalise);
        specialite.setDepartement(departement);

        Specialite updated = specialiteRepository.save(specialite);
        return toResponse(updated);
    }

    public void supprimer(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));

        if (Boolean.TRUE.equals(specialite.getArchive())) {
            return;
        }

        specialite.setArchive(true);
        specialiteRepository.save(specialite);

        List<com.example.gestion.des.stagiaires.entity.Competence> competences = competenceRepository
                .findBySpecialiteIdAndArchiveFalse(id);
        if (!competences.isEmpty()) {
            competences.forEach(c -> c.setArchive(true));
            competenceRepository.saveAll(competences);
        }
    }

    @Transactional
    public SpecialiteResponse desarchiver(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));

        if (!Boolean.TRUE.equals(specialite.getArchive())) {
            return toResponse(specialite);
        }

        specialite.setArchive(false);
        specialiteRepository.save(specialite);

        // Restore competences that were archived with this specialite.
        List<com.example.gestion.des.stagiaires.entity.Competence> competences = competenceRepository
                .findBySpecialiteIdAndArchiveTrue(id);
        if (!competences.isEmpty()) {
            competences.forEach(c -> c.setArchive(false));
            competenceRepository.saveAll(competences);
        }

        return toResponse(specialite);
    }

    @Transactional(readOnly = true)
    public List<SpecialiteResponse> listerToutes() {
        return specialiteRepository.findByArchiveFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SpecialiteResponse> listerParDepartement(UUID departementId) {
        return specialiteRepository.findByDepartementIdAndArchiveFalse(departementId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SpecialiteResponse> listerArchivesParDepartement(UUID departementId) {
        return specialiteRepository.findByDepartementIdAndArchiveTrue(departementId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SpecialiteResponse trouverParId(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));
        return toResponse(specialite);
    }

    public SpecialiteResponse toResponse(Specialite specialite) {
        return SpecialiteResponse.builder()
                .id(specialite.getId())
                .nom(specialite.getNom())
                .departementId(specialite.getDepartement() != null ? specialite.getDepartement().getId() : null)
                .departementNom(specialite.getDepartement() != null ? specialite.getDepartement().getNom() : null)
                .competences(specialite.getCompetences() != null
                        ? specialite.getCompetences().stream()
                                .map(competenceService::toResponse)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
