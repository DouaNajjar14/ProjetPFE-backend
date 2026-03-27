package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.CompetenceRequest;
import com.example.gestion.des.stagiaires.dto.CompetenceResponse;
import com.example.gestion.des.stagiaires.service.CompetenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompetenceController {

    private final CompetenceService competenceService;

    // ═══════════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS (No authentication required)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/api/public/competences")
    public ResponseEntity<List<CompetenceResponse>> listerPublic() {
        return ResponseEntity.ok(competenceService.listerToutes());
    }

    @GetMapping("/api/public/competences/specialite/{specialiteId}")
    public ResponseEntity<List<CompetenceResponse>> listerParSpecialitePublic(@PathVariable Long specialiteId) {
        return ResponseEntity.ok(competenceService.listerParSpecialite(specialiteId));
    }

    // ═══════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS (Authentication required)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/api/admin/competences")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetenceResponse> creer(@Valid @RequestBody CompetenceRequest request) {
        CompetenceResponse response = competenceService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/admin/competences/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetenceResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CompetenceRequest request) {
        CompetenceResponse response = competenceService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/admin/competences/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        competenceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/competences")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CompetenceResponse>> lister() {
        return ResponseEntity.ok(competenceService.listerToutes());
    }

    @GetMapping("/api/admin/competences/specialite/{specialiteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CompetenceResponse>> listerParSpecialite(@PathVariable Long specialiteId) {
        return ResponseEntity.ok(competenceService.listerParSpecialite(specialiteId));
    }

    @GetMapping("/api/admin/competences/specialite/{specialiteId}/archives")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CompetenceResponse>> listerArchivesParSpecialite(@PathVariable Long specialiteId) {
        return ResponseEntity.ok(competenceService.listerArchivesParSpecialite(specialiteId));
    }

    @PatchMapping("/api/admin/competences/{id}/archiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetenceResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.archiver(id));
    }

    @PatchMapping("/api/admin/competences/{id}/desarchiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompetenceResponse> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.desarchiver(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<CompetenceResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.trouverParId(id));
    }
}
