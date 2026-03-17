package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.DepartementRequest;
import com.example.gestion.des.stagiaires.dto.DepartementResponse;
import com.example.gestion.des.stagiaires.entity.Departement;
import com.example.gestion.des.stagiaires.repository.DepartementRepository;
import com.example.gestion.des.stagiaires.repository.EncadrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final EncadrantRepository encadrantRepository;

    public DepartementResponse creer(DepartementRequest request) {
        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom du département est obligatoire");
        }

        if (departementRepository.existsByNomIgnoreCase(nomNormalise)) {
            throw new RuntimeException("Un département avec ce nom existe déjà");
        }

        Departement departement = Departement.builder()
                .nom(nomNormalise)
                .responsable(request.getResponsable())
                .nombreEncadrantsActuel(0)
                .nombreStagiairesActuel(0)
                .archive(false)
                .build();

        Departement saved = departementRepository.save(departement);
        return toResponse(saved);
    }

    public DepartementResponse modifier(UUID id, DepartementRequest request) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec l'id : " + id));

        String nomNormalise = request.getNom() != null ? request.getNom().trim() : null;
        if (nomNormalise == null || nomNormalise.isBlank()) {
            throw new RuntimeException("Le nom du département est obligatoire");
        }

        if (departementRepository.existsByNomIgnoreCaseAndIdNot(nomNormalise, id)) {
            throw new RuntimeException("Un département avec ce nom existe déjà");
        }

        departement.setNom(nomNormalise);
        departement.setResponsable(request.getResponsable());
        Departement updated = departementRepository.save(departement);
        return toResponse(updated);
    }

    public DepartementResponse archiver(UUID id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec l'id : " + id));

        departement.setArchive(true);
        Departement archived = departementRepository.save(departement);
        return toResponse(archived);
    }

    public DepartementResponse desarchiver(UUID id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec l'id : " + id));

        departement.setArchive(false);
        Departement unarchived = departementRepository.save(departement);
        return toResponse(unarchived);
    }

    public List<DepartementResponse> listerActifs() {
        return departementRepository.findByArchiveFalse()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DepartementResponse> listerArchives() {
        return departementRepository.findByArchiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DepartementResponse> listerTous() {
        return departementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DepartementResponse trouverParId(UUID id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé avec l'id : " + id));
        return toResponse(departement);
    }

    private DepartementResponse toResponse(Departement departement) {
        // Calculer dynamiquement le nombre d'encadrants actifs dans ce département
        Long nombreEncadrants = encadrantRepository.countByDepartementIdAndActifTrue(departement.getId());

        return DepartementResponse.builder()
                .id(departement.getId())
                .nom(departement.getNom())
                .responsable(departement.getResponsable())
                .nombreEncadrantsActuel(nombreEncadrants != null ? nombreEncadrants.intValue() : 0)
                .nombreStagiairesActuel(departement.getNombreStagiairesActuel())
                .archive(departement.getArchive())
                .build();
    }
}
