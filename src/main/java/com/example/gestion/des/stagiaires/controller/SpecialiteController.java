package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.SpecialiteRequest;
import com.example.gestion.des.stagiaires.dto.SpecialiteResponse;
import com.example.gestion.des.stagiaires.service.SpecialiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteResponse> creer(@Valid @RequestBody SpecialiteRequest request) {
        SpecialiteResponse response = specialiteService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody SpecialiteRequest request) {
        SpecialiteResponse response = specialiteService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        specialiteService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<SpecialiteResponse>> lister() {
        return ResponseEntity.ok(specialiteService.listerToutes());
    }

    @GetMapping("/departement/{departementId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<SpecialiteResponse>> listerParDepartement(@PathVariable UUID departementId) {
        return ResponseEntity.ok(specialiteService.listerParDepartement(departementId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SpecialiteResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(specialiteService.trouverParId(id));
    }
}
