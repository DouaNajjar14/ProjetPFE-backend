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
@RequiredArgsConstructor
public class SpecialiteUniversitaireController {

    private final SpecialiteUniversitaireService service;

    // ═══════════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS (No authentication required)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/public/specialites-universitaires")
    public ResponseEntity<List<SpecialiteUniversitaireResponse>> listerPublic() {
        return ResponseEntity.ok(service.listerToutes());
    }

    @GetMapping("/api/public/specialites-universitaires/{id}")
    public ResponseEntity<SpecialiteUniversitaireResponse> trouverParIdPublic(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    // ═══════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS (Authentication required)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/admin/specialites-universitaires")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteUniversitaireResponse> creer(
            @Valid @RequestBody SpecialiteUniversitaireRequest request) {
        SpecialiteUniversitaireResponse response = service.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/admin/specialites-universitaires/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteUniversitaireResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody SpecialiteUniversitaireRequest request) {
        SpecialiteUniversitaireResponse response = service.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/admin/specialites-universitaires/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/specialites-universitaires")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<SpecialiteUniversitaireResponse>> lister() {
        return ResponseEntity.ok(service.listerToutes());
    }

    @GetMapping("/api/admin/specialites-universitaires/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SpecialiteUniversitaireResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }
}
