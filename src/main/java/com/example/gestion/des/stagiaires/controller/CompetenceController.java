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
@RequestMapping("/api/admin/competences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CompetenceController {

    private final CompetenceService competenceService;

    @PostMapping
    public ResponseEntity<CompetenceResponse> creer(@Valid @RequestBody CompetenceRequest request) {
        CompetenceResponse response = competenceService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenceResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody CompetenceRequest request) {
        CompetenceResponse response = competenceService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        competenceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CompetenceResponse>> lister() {
        return ResponseEntity.ok(competenceService.listerToutes());
    }

    @GetMapping("/specialite/{specialiteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CompetenceResponse>> listerParSpecialite(@PathVariable Long specialiteId) {
        return ResponseEntity.ok(competenceService.listerParSpecialite(specialiteId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<CompetenceResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(competenceService.trouverParId(id));
    }
}
