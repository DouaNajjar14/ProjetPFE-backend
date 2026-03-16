package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.SpecialiteUniversitaireRequest;
import com.example.gestion.des.stagiaires.dto.SpecialiteUniversitaireResponse;
import com.example.gestion.des.stagiaires.service.SpecialiteUniversitaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/specialites-universitaires")
@RequiredArgsConstructor
public class SpecialiteUniversitaireController {

    private final SpecialiteUniversitaireService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteUniversitaireResponse> creer(
            @Valid @RequestBody SpecialiteUniversitaireRequest request) {
        SpecialiteUniversitaireResponse response = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteUniversitaireResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody SpecialiteUniversitaireRequest request) {
        SpecialiteUniversitaireResponse response = service.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<SpecialiteUniversitaireResponse>> lister() {
        return ResponseEntity.ok(service.listerToutes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SpecialiteUniversitaireResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }
}
