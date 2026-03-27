package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.DocumentUrlResponse;
import com.example.gestion.des.stagiaires.service.DocumentCandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents/candidatures")
@RequiredArgsConstructor
public class DocumentCandidatureController {

    private final DocumentCandidatureService documentService;

    /**
     * Upload CV for a candidate
     */
    @PostMapping("/{candidatId}/cv")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> uploadCv(
            @PathVariable UUID candidatId,
            @RequestParam("file") MultipartFile file) {

        DocumentUrlResponse response = documentService.uploadCv(candidatId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Upload lettre de motivation for a candidate
     */
    @PostMapping("/{candidatId}/lettre")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> uploadLettreMotivation(
            @PathVariable UUID candidatId,
            @RequestParam("file") MultipartFile file) {

        DocumentUrlResponse response = documentService.uploadLettreMotivation(candidatId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a read-only URL for CV (inline viewing in iframe/PDF viewer)
     */
    @GetMapping("/{candidatId}/cv/lecture")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ENCADRANT', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> getCvLectureUrl(
            @PathVariable UUID candidatId,
            Authentication authentication) {

        DocumentUrlResponse response = documentService.getUrlLectureCv(candidatId, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a download URL for CV
     */
    @GetMapping("/{candidatId}/cv/telechargement")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ENCADRANT', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> getCvTelechargementUrl(
            @PathVariable UUID candidatId,
            Authentication authentication) {

        DocumentUrlResponse response = documentService.getUrlTelechargementCv(candidatId, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a read-only URL for lettre de motivation
     */
    @GetMapping("/{candidatId}/lettre/lecture")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ENCADRANT', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> getLettreLectureUrl(
            @PathVariable UUID candidatId,
            Authentication authentication) {

        DocumentUrlResponse response = documentService.getUrlLectureLettre(candidatId, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a download URL for lettre de motivation
     */
    @GetMapping("/{candidatId}/lettre/telechargement")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ENCADRANT', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> getLettreTelechargementUrl(
            @PathVariable UUID candidatId,
            Authentication authentication) {

        DocumentUrlResponse response = documentService.getUrlTelechargementLettre(candidatId, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Get download URL for any document type (supports both CV and
     * LETTRE_MOTIVATION)
     * Path: /api/documents/candidatures/{candidatId}/{documentType}/telechargement
     * documentType can be "CV" or "LETTRE_MOTIVATION"
     */
    @GetMapping("/{candidatId}/{documentType}/telechargement")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'AGENT_RH', 'ENCADRANT', 'ADMIN')")
    public ResponseEntity<DocumentUrlResponse> getDocumentTelechargement(
            @PathVariable UUID candidatId,
            @PathVariable String documentType,
            Authentication authentication) {

        DocumentUrlResponse response;
        if ("CV".equalsIgnoreCase(documentType)) {
            response = documentService.getUrlTelechargementCv(candidatId, authentication);
        } else if ("LETTRE_MOTIVATION".equalsIgnoreCase(documentType)) {
            response = documentService.getUrlTelechargementLettre(candidatId, authentication);
        } else {
            throw new IllegalArgumentException("Type de document invalide: " + documentType);
        }
        return ResponseEntity.ok(response);
    }
}
