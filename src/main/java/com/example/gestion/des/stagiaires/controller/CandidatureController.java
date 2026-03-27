package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.CandidatureRequest;
import com.example.gestion.des.stagiaires.dto.CandidatureResponse;
import com.example.gestion.des.stagiaires.dto.CandidatureUpdateRequest;
import com.example.gestion.des.stagiaires.dto.StatutCandidatureRequest;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.service.CandidatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidatureResponse> creer(
            @RequestPart("candidature") String candidatureJson,
            @RequestPart("cv1") MultipartFile cv1,
            @RequestPart(value = "lettreMotivation1", required = false) MultipartFile lettreMotivation1,
            @RequestPart(value = "cv2", required = false) MultipartFile cv2,
            @RequestPart(value = "lettreMotivation2", required = false) MultipartFile lettreMotivation2) {
        try {
            CandidatureRequest request = objectMapper.readValue(candidatureJson, CandidatureRequest.class);
            CandidatureResponse response = candidatureService.creer(request, cv1, lettreMotivation1, cv2,
                    lettreMotivation2);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la candidature: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidatureResponse> modifier(
            @PathVariable UUID id,
            @RequestBody CandidatureUpdateRequest request) {
        CandidatureResponse response = candidatureService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint dédié au changement de statut d'une candidature.
     * Déclenche automatiquement le webhook n8n pour l'envoi d'emails :
     * - PRESELECTIONNE : n8n crée un Google Meet basé sur dateEntretien
     * (OBLIGATOIRE)
     * puis envoie l'email avec le lien Meet au(x) candidat(s)
     * - ACCEPTE : n8n envoie un email de félicitations
     * - REJETE : n8n envoie un email de refus
     *
     * Body JSON exemple (PRESELECTIONNE) :
     * {
     * "statut": "PRESELECTIONNE",
     * "dateEntretien": "2026-04-15T10:30:00"
     * }
     *
     * Body JSON exemple (ACCEPTE / REJETE) :
     * {
     * "statut": "ACCEPTE"
     * }
     */
    @PatchMapping("/{id}/statut")
    public ResponseEntity<CandidatureResponse> changerStatut(
            @PathVariable UUID id,
            @RequestBody StatutCandidatureRequest request) {
        CandidatureResponse response = candidatureService.changerStatut(id, request);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PHASE 1 — Workflows RH
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Présélectionner une candidature
     * Transition : EN_ATTENTE → PRESELECTIONNE
     * Endpoint : POST /api/candidatures/{id}/preselectionner
     *
     * Exemple :
     * curl -X POST http://localhost:8080/api/candidatures/{id}/preselectionner
     */
    @PostMapping("/{id}/preselectionner")
    public ResponseEntity<CandidatureResponse> preselectionner(@PathVariable UUID id) {
        CandidatureResponse response = candidatureService.preselectionner(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Planifier l'entretien d'une candidature
     * Transition : PRESELECTIONNE → ENTRETIEN
     * Endpoint : POST /api/candidatures/{id}/planifier-entretien
     *
     * Body JSON :
     * {
     * "dateEntretien": "2026-04-20T14:00:00"
     * }
     *
     * n8n sera notifié pour :
     * 1. Créer un Google Meet automatiquement
     * 2. Envoyer l'email avec le lien Meet au candidat
     */
    @PostMapping("/{id}/planifier-entretien")
    public ResponseEntity<CandidatureResponse> planiferEntretien(
            @PathVariable UUID id,
            @RequestBody CandidatureUpdateRequest request) {
        CandidatureResponse response = candidatureService.planiferEntretien(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Accepter une candidature après entretien
     * Transition : ENTRETIEN → ACCEPTE
     * Endpoint : POST /api/candidatures/{id}/accepter
     *
     * Body JSON :
     * {
     * "dateDebut": "2026-07-01",
     * "dateFin": "2026-09-01"
     * }
     *
     * n8n sera notifié pour envoyer l'email d'acceptation avec les dates du stage
     */
    @PostMapping("/{id}/accepter")
    public ResponseEntity<CandidatureResponse> accepterCandidature(
            @PathVariable UUID id,
            @RequestBody CandidatureUpdateRequest request) {
        CandidatureResponse response = candidatureService.accepterCandidature(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refuser une candidature
     * Transition : * → REFUSE (depuis n'importe quel état)
     * Endpoint : POST /api/candidatures/{id}/refuser
     *
     * n8n sera notifié pour envoyer l'email de refus au candidat
     *
     * Exemple :
     * curl -X POST http://localhost:8080/api/candidatures/{id}/refuser
     */
    @PostMapping("/{id}/refuser")
    public ResponseEntity<CandidatureResponse> refuserCandidature(@PathVariable UUID id) {
        CandidatureResponse response = candidatureService.refuserCandidature(id);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // Endpoints de consultation
    // ─────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<CandidatureResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(candidatureService.trouverParId(id));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CandidatureResponse>> listerParStatut(@PathVariable StatutCandidature statut) {
        return ResponseEntity.ok(candidatureService.listerParStatut(statut));
    }

    @GetMapping("/type/{typeStage}")
    public ResponseEntity<List<CandidatureResponse>> listerParTypeStage(@PathVariable TypeStage typeStage) {
        return ResponseEntity.ok(candidatureService.listerParTypeStage(typeStage));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();

        // Statistiques par statut
        Map<String, Long> parStatut = new HashMap<>();
        for (StatutCandidature statut : StatutCandidature.values()) {
            parStatut.put(statut.name(), candidatureService.compterParStatut(statut));
        }
        stats.put("parStatut", parStatut);

        // Statistiques par type de stage
        Map<String, Long> parType = new HashMap<>();
        for (TypeStage type : TypeStage.values()) {
            parType.put(type.name(), candidatureService.compterParTypeStage(type));
        }
        stats.put("parTypeStage", parType);

        return ResponseEntity.ok(stats);
    }
}
