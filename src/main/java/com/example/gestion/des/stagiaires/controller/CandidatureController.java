package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.CandidatureRequest;
import com.example.gestion.des.stagiaires.dto.CandidatureResponse;
import com.example.gestion.des.stagiaires.dto.CandidatureUpdateRequest;
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

    @GetMapping
    public ResponseEntity<List<CandidatureResponse>> listerTous() {
        return ResponseEntity.ok(candidatureService.listerTous());
    }

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
