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
        if (specialiteRepository.existsByNomAndDepartementId(request.getNom(), request.getDepartementId())) {
            throw new RuntimeException("Une spécialité avec ce nom existe déjà dans ce département");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        Specialite specialite = Specialite.builder()
                .nom(request.getNom())
                .departement(departement)
                .competences(new ArrayList<>())
                .build();

        Specialite saved = specialiteRepository.save(specialite);
        return toResponse(saved);
    }

    @Transactional
    public SpecialiteResponse modifier(Long id, SpecialiteRequest request) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));

        // Vérifier si le nom est déjà utilisé par une autre spécialité dans le même
        // département
        if (!specialite.getNom().equals(request.getNom())
                && specialiteRepository.existsByNomAndDepartementId(request.getNom(), request.getDepartementId())) {
            throw new RuntimeException("Une spécialité avec ce nom existe déjà dans ce département");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        specialite.setNom(request.getNom());
        specialite.setDepartement(departement);

        Specialite updated = specialiteRepository.save(specialite);
        return toResponse(updated);
    }

    public void supprimer(Long id) {
        if (!specialiteRepository.existsById(id)) {
            throw new RuntimeException("Spécialité non trouvée avec l'id : " + id);
        }
        specialiteRepository.deleteById(id);
    }

    public SpecialiteResponse archiver(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));
        specialite.setArchive(true);
        Specialite updated = specialiteRepository.save(specialite);
        return toResponse(updated);
    }

    public SpecialiteResponse desarchiver(Long id) {
        Specialite specialite = specialiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité non trouvée avec l'id : " + id));
        specialite.setArchive(false);
        Specialite updated = specialiteRepository.save(specialite);
        return toResponse(updated);
    }

    public List<SpecialiteResponse> listerToutes() {
        return specialiteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SpecialiteResponse> listerParDepartement(UUID departementId) {
        return specialiteRepository.findByDepartementId(departementId)
                .stream()
                .filter(s -> !Boolean.TRUE.equals(s.getArchive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SpecialiteResponse> listerArchivesParDepartement(UUID departementId) {
        return specialiteRepository.findByDepartementId(departementId)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getArchive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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
                .archive(Boolean.TRUE.equals(specialite.getArchive()))
                .competences(specialite.getCompetences() != null
                        ? specialite.getCompetences().stream()
                                .map(competenceService::toResponse)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
