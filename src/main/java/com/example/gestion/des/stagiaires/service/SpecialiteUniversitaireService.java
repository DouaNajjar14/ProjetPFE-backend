package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.SpecialiteUniversitaireRequest;
import com.example.gestion.des.stagiaires.dto.SpecialiteUniversitaireResponse;
import com.example.gestion.des.stagiaires.entity.SpecialiteUniversitaire;
import com.example.gestion.des.stagiaires.repository.SpecialiteUniversitaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecialiteUniversitaireService {

    private final SpecialiteUniversitaireRepository repository;

    @Transactional
    public SpecialiteUniversitaireResponse creer(SpecialiteUniversitaireRequest request) {
        if (repository.existsByNom(request.getNom())) {
            throw new RuntimeException("Une spécialité universitaire avec ce nom existe déjà");
        }

        SpecialiteUniversitaire specialite = SpecialiteUniversitaire.builder()
                .nom(request.getNom())
                .build();

        SpecialiteUniversitaire saved = repository.save(specialite);
        return toResponse(saved);
    }

    @Transactional
    public SpecialiteUniversitaireResponse modifier(Long id, SpecialiteUniversitaireRequest request) {
        SpecialiteUniversitaire specialite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité universitaire non trouvée avec l'id : " + id));

        // Vérifier si le nom est déjà utilisé par une autre spécialité
        if (!specialite.getNom().equals(request.getNom()) && repository.existsByNom(request.getNom())) {
            throw new RuntimeException("Une spécialité universitaire avec ce nom existe déjà");
        }

        specialite.setNom(request.getNom());

        SpecialiteUniversitaire updated = repository.save(specialite);
        return toResponse(updated);
    }

    public void supprimer(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Spécialité universitaire non trouvée avec l'id : " + id);
        }
        repository.deleteById(id);
    }

    public List<SpecialiteUniversitaireResponse> listerToutes() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SpecialiteUniversitaireResponse trouverParId(Long id) {
        SpecialiteUniversitaire specialite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spécialité universitaire non trouvée avec l'id : " + id));
        return toResponse(specialite);
    }

    public List<SpecialiteUniversitaire> trouverParIds(List<Long> ids) {
        return repository.findAllByIdIn(ids);
    }

    private SpecialiteUniversitaireResponse toResponse(SpecialiteUniversitaire specialite) {
        return SpecialiteUniversitaireResponse.builder()
                .id(specialite.getId())
                .nom(specialite.getNom())
                .build();
    }
}
