package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.CandidatureUpdateRequest;
import com.example.gestion.des.stagiaires.dto.CandidatureResponse;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent-rh/candidatures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
public class AgentRHCandidatureController {

    private final CandidatureService candidatureService;

    @GetMapping
    public ResponseEntity<List<CandidatureResponse>> listerToutes() {
        return ResponseEntity.ok(candidatureService.listerTous());
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();

        Map<String, Long> parStatut = new HashMap<>();
        for (StatutCandidature statut : StatutCandidature.values()) {
            parStatut.put(statut.name(), candidatureService.compterParStatut(statut));
        }
        stats.put("parStatut", parStatut);

        Map<String, Long> parType = new HashMap<>();
        for (TypeStage type : TypeStage.values()) {
            parType.put(type.name(), candidatureService.compterParTypeStage(type));
        }
        stats.put("parTypeStage", parType);

        // Add total count
        long total = 0;
        for (Long count : parStatut.values()) {
            total += count;
        }
        stats.put("total", total);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CandidatureResponse>> listerParStatut(@PathVariable StatutCandidature statut) {
        return ResponseEntity.ok(candidatureService.listerParStatut(statut));
    }

    @GetMapping("/type/{typeStage}")
    public ResponseEntity<List<CandidatureResponse>> listerParTypeStage(@PathVariable TypeStage typeStage) {
        return ResponseEntity.ok(candidatureService.listerParTypeStage(typeStage));
    }

    @GetMapping("/type/{typeStage}/statut/{statut}")
    public ResponseEntity<List<CandidatureResponse>> listerParTypeEtStatut(
            @PathVariable TypeStage typeStage,
            @PathVariable StatutCandidature statut) {
        // Since service method might not exist for combined filter, filtering in memory
        // or add to service
        // For now, let's filter the larger list or use specific service method if
        // exists.
        // I will assume for now I need to filter.
        List<CandidatureResponse> byType = candidatureService.listerParTypeStage(typeStage);
        List<CandidatureResponse> filtered = byType.stream()
                .filter(c -> c.getStatut() == statut)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidatureResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(candidatureService.trouverParId(id));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<CandidatureResponse> changerStatut(
            @PathVariable UUID id,
            @RequestBody CandidatureUpdateRequest request) {
        // We reuse the existing modifier method from service which takes the update
        // request
        return ResponseEntity.ok(candidatureService.modifier(id, request));
    }
}
