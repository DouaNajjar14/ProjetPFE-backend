package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.EncadrantRequest;
import com.example.gestion.des.stagiaires.dto.EncadrantResponse;
import com.example.gestion.des.stagiaires.dto.EncadrantUpdateRequest;
import com.example.gestion.des.stagiaires.entity.Competence;
import com.example.gestion.des.stagiaires.entity.Departement;
import com.example.gestion.des.stagiaires.entity.Encadrant;
import com.example.gestion.des.stagiaires.entity.Specialite;
import com.example.gestion.des.stagiaires.enums.Role;
import com.example.gestion.des.stagiaires.repository.CompetenceRepository;
import com.example.gestion.des.stagiaires.repository.DepartementRepository;
import com.example.gestion.des.stagiaires.repository.EncadrantRepository;
import com.example.gestion.des.stagiaires.repository.SpecialiteRepository;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EncadrantService {

    private final EncadrantRepository encadrantRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;
    private final SpecialiteRepository specialiteRepository;
    private final CompetenceRepository competenceRepository;
    private final SpecialiteService specialiteService;
    private final CompetenceService competenceService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EncadrantResponse creer(EncadrantRequest request) {
        if (request.getDepartementId() == null) {
            throw new RuntimeException("Le département est obligatoire");
        }

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        Encadrant encadrant = new Encadrant();
        encadrant.setNom(request.getNom());
        encadrant.setPrenom(request.getPrenom());
        encadrant.setEmail(request.getEmail());
        encadrant.setTel(request.getTel());
        encadrant.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        encadrant.setRole(Role.ENCADRANT);
        encadrant.setActif(true);
        encadrant.setCapaciteMax(request.getCapaciteMax());
        encadrant.setCapaciteActuelle(0);
        encadrant.setDepartement(departement);
        encadrant.setSpecialites(new HashSet<>());
        encadrant.setCompetences(new HashSet<>());

        // Ajouter les spécialités si fournies
        if (request.getSpecialiteIds() != null && !request.getSpecialiteIds().isEmpty()) {
            List<Specialite> specialites = specialiteRepository.findAllById(request.getSpecialiteIds());
            encadrant.setSpecialites(new HashSet<>(specialites));
        }

        // Ajouter les compétences si fournies
        if (request.getCompetenceIds() != null && !request.getCompetenceIds().isEmpty()) {
            List<Competence> competences = competenceRepository.findByIdIn(request.getCompetenceIds());
            encadrant.setCompetences(new HashSet<>(competences));
        }

        Encadrant saved = encadrantRepository.saveAndFlush(encadrant);
        return toResponse(saved);
    }

    @Transactional
    public EncadrantResponse modifier(UUID id, EncadrantUpdateRequest request) {
        if (request.getDepartementId() == null) {
            throw new RuntimeException("Le département est obligatoire");
        }

        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'id : " + id));

        if (!encadrant.getEmail().equals(request.getEmail())
                && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(
                        () -> new RuntimeException("Département non trouvé avec l'id : " + request.getDepartementId()));

        encadrant.setNom(request.getNom());
        encadrant.setPrenom(request.getPrenom());
        encadrant.setEmail(request.getEmail());
        encadrant.setTel(request.getTel());
        encadrant.setCapaciteMax(request.getCapaciteMax());
        encadrant.setDepartement(departement);

        // Mettre à jour les spécialités
        if (request.getSpecialiteIds() != null) {
            List<Specialite> specialites = specialiteRepository.findAllById(request.getSpecialiteIds());
            encadrant.setSpecialites(new HashSet<>(specialites));
        } else {
            encadrant.getSpecialites().clear();
        }

        // Mettre à jour les compétences
        if (request.getCompetenceIds() != null) {
            List<Competence> competences = competenceRepository.findByIdIn(request.getCompetenceIds());
            encadrant.setCompetences(new HashSet<>(competences));
        } else {
            encadrant.getCompetences().clear();
        }

        Encadrant updated = encadrantRepository.save(encadrant);
        return toResponse(updated);
    }

    public EncadrantResponse archiver(UUID id) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'id : " + id));

        encadrant.setActif(false);
        Encadrant archived = encadrantRepository.save(encadrant);
        return toResponse(archived);
    }

    public EncadrantResponse desarchiver(UUID id) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'id : " + id));

        encadrant.setActif(true);
        Encadrant unarchived = encadrantRepository.save(encadrant);
        return toResponse(unarchived);
    }

    public List<EncadrantResponse> listerActifs() {
        return encadrantRepository.findByActif(true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EncadrantResponse> listerArchives() {
        return encadrantRepository.findByActif(false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EncadrantResponse> listerTous() {
        return encadrantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EncadrantResponse trouverParId(UUID id) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'id : " + id));
        return toResponse(encadrant);
    }

    public List<EncadrantResponse> listerDisponibles() {
        return encadrantRepository.findByActif(true)
                .stream()
                .filter(e -> e.getCapaciteActuelle() < e.getCapaciteMax())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EncadrantResponse modifierCapacite(UUID id, Integer nouvelleCapacite) {
        Encadrant encadrant = encadrantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encadrant non trouvé avec l'id : " + id));

        if (nouvelleCapacite < encadrant.getCapaciteActuelle()) {
            throw new RuntimeException("La nouvelle capacité ne peut pas être inférieure à la capacité actuelle ("
                    + encadrant.getCapaciteActuelle() + ")");
        }

        encadrant.setCapaciteMax(nouvelleCapacite);
        Encadrant updated = encadrantRepository.save(encadrant);
        return toResponse(updated);
    }

    private EncadrantResponse toResponse(Encadrant encadrant) {
        return EncadrantResponse.builder()
                .id(encadrant.getId())
                .nom(encadrant.getNom())
                .prenom(encadrant.getPrenom())
                .email(encadrant.getEmail())
                .tel(encadrant.getTel())
                .capaciteMax(encadrant.getCapaciteMax())
                .capaciteActuelle(encadrant.getCapaciteActuelle())
                .actif(encadrant.getActif())
                .dateCreation(encadrant.getDateCreation())
                .dateModification(encadrant.getDateModification())
                .departementId(encadrant.getDepartement() != null ? encadrant.getDepartement().getId() : null)
                .departementNom(encadrant.getDepartement() != null ? encadrant.getDepartement().getNom() : null)
                .specialites(encadrant.getSpecialites() != null
                        ? encadrant.getSpecialites().stream()
                                .map(specialiteService::toResponse)
                                .collect(Collectors.toList())
                        : List.of())
                .competences(encadrant.getCompetences() != null
                        ? encadrant.getCompetences().stream()
                                .map(competenceService::toResponse)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
