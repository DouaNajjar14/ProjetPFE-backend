package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.UniversiteRequest;
import com.example.gestion.des.stagiaires.dto.UniversiteResponse;
import com.example.gestion.des.stagiaires.entity.Universite;
import com.example.gestion.des.stagiaires.repository.UniversiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversiteService {

    private final UniversiteRepository universiteRepository;

    public UniversiteResponse creer(UniversiteRequest request) {
        if (universiteRepository.existsByNom(request.getNom())) {
            throw new RuntimeException("Une université avec ce nom existe déjà");
        }

        Universite universite = Universite.builder()
                .nom(request.getNom())
                .build();

        Universite saved = universiteRepository.save(universite);
        return toResponse(saved);
    }

    public UniversiteResponse modifier(UUID id, UniversiteRequest request) {
        Universite universite = universiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Université non trouvée avec l'id : " + id));

        universite.setNom(request.getNom());
        Universite updated = universiteRepository.save(universite);
        return toResponse(updated);
    }

    public void supprimer(UUID id) {
        if (!universiteRepository.existsById(id)) {
            throw new RuntimeException("Université non trouvée avec l'id : " + id);
        }
        universiteRepository.deleteById(id);
    }

    public List<UniversiteResponse> listerTous() {
        return universiteRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UniversiteResponse trouverParId(UUID id) {
        Universite universite = universiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Université non trouvée avec l'id : " + id));
        return toResponse(universite);
    }

    private UniversiteResponse toResponse(Universite universite) {
        return UniversiteResponse.builder()
                .id(universite.getId())
                .nom(universite.getNom())
                .build();
    }
}
