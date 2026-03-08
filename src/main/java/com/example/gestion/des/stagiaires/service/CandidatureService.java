package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.*;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.entity.SujetPfe;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.CandidatureRepository;
import com.example.gestion.des.stagiaires.repository.SujetPfeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatService candidatService;
    private final SujetPfeRepository sujetPfeRepository;

    @Transactional
    public CandidatureResponse creer(CandidatureRequest request,
            MultipartFile cv1, MultipartFile lettreMotivation1,
            MultipartFile cv2, MultipartFile lettreMotivation2) {
        // Validation des contraintes métier
        validateCandidature(request);

        // Créer ou trouver le candidat principal
        Candidat candidat1 = candidatService.creerOuTrouver(request.getCandidat1(), cv1, lettreMotivation1);

        // Créer ou trouver le candidat binôme si nécessaire
        Candidat candidat2 = null;
        if (Boolean.TRUE.equals(request.getEstBinome()) && request.getCandidat2() != null) {
            candidat2 = candidatService.creerOuTrouver(request.getCandidat2(), cv2, lettreMotivation2);
        }

        // Récupérer les sujets PFE si nécessaire
        SujetPfe sujetChoix1 = null;
        SujetPfe sujetChoix2 = null;
        if (request.getTypeStage() == TypeStage.PFE) {
            if (request.getSujetChoix1Id() != null) {
                sujetChoix1 = sujetPfeRepository.findById(request.getSujetChoix1Id())
                        .orElseThrow(() -> new RuntimeException("Sujet PFE choix 1 non trouvé"));
            }
            if (request.getSujetChoix2Id() != null) {
                sujetChoix2 = sujetPfeRepository.findById(request.getSujetChoix2Id())
                        .orElseThrow(() -> new RuntimeException("Sujet PFE choix 2 non trouvé"));
            }
        }

        // Créer la candidature
        Candidature candidature = Candidature.builder()
                .typeStage(request.getTypeStage())
                .statut(StatutCandidature.EN_ATTENTE)
                .estBinome(request.getEstBinome())
                .candidat1(candidat1)
                .candidat2(candidat2)
                .sujetChoix1(sujetChoix1)
                .sujetChoix2(sujetChoix2)
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .build();

        Candidature saved = candidatureRepository.save(candidature);
        return toResponse(saved);
    }

    private void validateCandidature(CandidatureRequest request) {
        if (request.getTypeStage() == TypeStage.PFE) {
            if (request.getSujetChoix1Id() == null) {
                throw new RuntimeException("Le choix d'un sujet PFE est obligatoire pour un stage PFE");
            }
            if (request.getSujetChoix2Id() != null &&
                    request.getSujetChoix1Id().equals(request.getSujetChoix2Id())) {
                throw new RuntimeException("Les deux choix de sujets PFE doivent être différents");
            }
        } else {
            if (request.getSujetChoix1Id() != null || request.getSujetChoix2Id() != null) {
                throw new RuntimeException("Les sujets PFE ne sont pas requis pour ce type de stage");
            }
        }

        if (Boolean.TRUE.equals(request.getEstBinome()) && request.getCandidat2() == null) {
            throw new RuntimeException("Les informations du binôme sont obligatoires");
        }
    }

    public CandidatureResponse modifier(UUID id, CandidatureUpdateRequest request) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));

        candidature.setStatut(request.getStatut());
        if (request.getDateEntretien() != null) {
            candidature.setDateEntretien(request.getDateEntretien());
        }

        Candidature updated = candidatureRepository.save(candidature);
        return toResponse(updated);
    }

    public List<CandidatureResponse> listerTous() {
        return candidatureRepository.findAllOrderByDateDepotDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerParStatut(StatutCandidature statut) {
        return candidatureRepository.findByStatut(statut)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerParTypeStage(TypeStage typeStage) {
        return candidatureRepository.findByTypeStage(typeStage)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CandidatureResponse trouverParId(UUID id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));
        return toResponse(candidature);
    }

    public Long compterParStatut(StatutCandidature statut) {
        return candidatureRepository.countByStatut(statut);
    }

    public Long compterParTypeStage(TypeStage typeStage) {
        return candidatureRepository.countByTypeStage(typeStage);
    }

    private CandidatureResponse toResponse(Candidature candidature) {
        return CandidatureResponse.builder()
                .id(candidature.getId())
                .typeStage(candidature.getTypeStage())
                .statut(candidature.getStatut())
                .estBinome(candidature.getEstBinome())
                .dateDepot(candidature.getDateDepot())
                .dateEntretien(candidature.getDateEntretien())
                .dateDebut(candidature.getDateDebut())
                .dateFin(candidature.getDateFin())
                .candidat1(candidatService.toResponse(candidature.getCandidat1()))
                .candidat2(candidature.getCandidat2() != null
                        ? candidatService.toResponse(candidature.getCandidat2())
                        : null)
                .sujetChoix1(candidature.getSujetChoix1() != null
                        ? toSujetPfeSimpleResponse(candidature.getSujetChoix1())
                        : null)
                .sujetChoix2(candidature.getSujetChoix2() != null
                        ? toSujetPfeSimpleResponse(candidature.getSujetChoix2())
                        : null)
                .build();
    }

    private SujetPfeSimpleResponse toSujetPfeSimpleResponse(SujetPfe sujet) {
        return SujetPfeSimpleResponse.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .departementNom(sujet.getDepartement().getNom())
                .build();
    }
}
