package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.CandidatRequest;
import com.example.gestion.des.stagiaires.dto.CandidatResponse;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.entity.Universite;
import com.example.gestion.des.stagiaires.repository.CandidatRepository;
import com.example.gestion.des.stagiaires.repository.UniversiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;
    private final UniversiteRepository universiteRepository;
    private final FileStorageService fileStorageService;

    public Candidat creerOuTrouver(CandidatRequest request, MultipartFile cv, MultipartFile lettreMotivation) {
        // Vérifier si le candidat existe déjà
        return candidatRepository.findByEmail(request.getEmail())
                .orElseGet(() -> creerNouveauCandidat(request, cv, lettreMotivation));
    }

    private Candidat creerNouveauCandidat(CandidatRequest request, MultipartFile cv, MultipartFile lettreMotivation) {
        Universite universite = universiteRepository.findById(request.getUniversiteId())
                .orElseThrow(() -> new RuntimeException("Université non trouvée"));

        String cvPath = fileStorageService.storeFile(cv, "cv");
        String lettrePath = lettreMotivation != null && !lettreMotivation.isEmpty()
                ? fileStorageService.storeFile(lettreMotivation, "lettres")
                : null;

        Candidat candidat = Candidat.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .tel(request.getTel())
                .niveauAcademique(request.getNiveauAcademique())
                .cv(cvPath)
                .lettreMotivation(lettrePath)
                .universite(universite)
                .build();

        return candidatRepository.save(candidat);
    }

    public List<CandidatResponse> listerTous() {
        return candidatRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CandidatResponse trouverParId(UUID id) {
        Candidat candidat = candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'id : " + id));
        return toResponse(candidat);
    }

    public CandidatResponse toResponse(Candidat candidat) {
        return CandidatResponse.builder()
                .id(candidat.getId())
                .nom(candidat.getNom())
                .prenom(candidat.getPrenom())
                .email(candidat.getEmail())
                .tel(candidat.getTel())
                .niveauAcademique(candidat.getNiveauAcademique())
                .cv(candidat.getCv())
                .lettreMotivation(candidat.getLettreMotivation())
                .universiteId(candidat.getUniversite().getId())
                .universiteNom(candidat.getUniversite().getNom())
                .build();
    }
}
