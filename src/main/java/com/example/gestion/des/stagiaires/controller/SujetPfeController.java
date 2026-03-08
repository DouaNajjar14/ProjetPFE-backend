package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.SujetPfeRequest;
import com.example.gestion.des.stagiaires.dto.SujetPfeResponse;
import com.example.gestion.des.stagiaires.enums.STATUT;
import com.example.gestion.des.stagiaires.service.SujetPfeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sujets-pfe")
@RequiredArgsConstructor
public class SujetPfeController {

    private final SujetPfeService sujetPfeService;

    // ===================== OPÉRATIONS AGENT RH (+ ADMIN) =====================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SujetPfeResponse> creer(@Valid @RequestBody SujetPfeRequest request) {
        SujetPfeResponse response = sujetPfeService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SujetPfeResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody SujetPfeRequest request) {
        SujetPfeResponse response = sujetPfeService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SujetPfeResponse> fermer(@PathVariable UUID id) {
        SujetPfeResponse response = sujetPfeService.fermer(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SujetPfeResponse> archiver(@PathVariable UUID id) {
        SujetPfeResponse response = sujetPfeService.archiver(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desarchiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<SujetPfeResponse> desarchiver(@PathVariable UUID id) {
        SujetPfeResponse response = sujetPfeService.desarchiver(id);
        return ResponseEntity.ok(response);
    }

    // ===================== CONSULTATION (tous les utilisateurs authentifiés)
    // =====================

    @GetMapping
    public ResponseEntity<Page<SujetPfeResponse>> listerActifs(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(sujetPfeService.listerActifs(pageable));
    }

    @GetMapping("/archives")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<Page<SujetPfeResponse>> listerArchives(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(sujetPfeService.listerArchives(pageable));
    }

    @GetMapping("/tous")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<Page<SujetPfeResponse>> listerTous(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(sujetPfeService.listerTous(pageable));
    }

    @GetMapping("/recherche")
    public ResponseEntity<Page<SujetPfeResponse>> rechercher(
            @RequestParam(required = false) STATUT statut,
            @RequestParam(required = false) UUID departementId,
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) Long specialiteId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(sujetPfeService.rechercher(statut, departementId, titre, specialiteId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SujetPfeResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(sujetPfeService.trouverParId(id));
    }
}
