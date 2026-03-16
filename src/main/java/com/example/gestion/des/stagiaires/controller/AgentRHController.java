package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.AgentRHRequest;
import com.example.gestion.des.stagiaires.dto.AgentRHResponse;
import com.example.gestion.des.stagiaires.dto.AgentRHUpdateRequest;
import com.example.gestion.des.stagiaires.dto.CandidatureResponse;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.service.AgentRHService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent-rh")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AgentRHController {

    private final AgentRHService agentRHService;

    // ─────────────────────────────────────────────────────────────────────────
    //  Gestion des agents RH (ADMIN uniquement)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRHResponse> creer(@Valid @RequestBody AgentRHRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentRHService.creer(request));
    }

    @PutMapping("/agents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRHResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody AgentRHUpdateRequest request) {
        return ResponseEntity.ok(agentRHService.modifier(id, request));
    }

    @PatchMapping("/agents/{id}/archiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRHResponse> archiver(@PathVariable UUID id) {
        return ResponseEntity.ok(agentRHService.archiver(id));
    }

    @PatchMapping("/agents/{id}/desarchiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRHResponse> desarchiver(@PathVariable UUID id) {
        return ResponseEntity.ok(agentRHService.desarchiver(id));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentRHResponse>> listerActifs() {
        return ResponseEntity.ok(agentRHService.listerActifs());
    }

    @GetMapping("/agents/archives")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentRHResponse>> listerArchives() {
        return ResponseEntity.ok(agentRHService.listerArchives());
    }

    @GetMapping("/agents/tous")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgentRHResponse>> listerTous() {
        return ResponseEntity.ok(agentRHService.listerTous());
    }

    @GetMapping("/agents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentRHResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(agentRHService.trouverParId(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Consultation des candidatures (ADMIN + AGENT_RH)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/candidatures")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CandidatureResponse>> listerToutesCandidatures() {
        return ResponseEntity.ok(agentRHService.listerToutesCandidatures());
    }

    @GetMapping("/candidatures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<CandidatureResponse> trouverCandidatureParId(@PathVariable UUID id) {
        return ResponseEntity.ok(agentRHService.trouverCandidatureParId(id));
    }

    @GetMapping("/candidatures/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CandidatureResponse>> listerParStatut(@PathVariable StatutCandidature statut) {
        return ResponseEntity.ok(agentRHService.listerCandidaturesParStatut(statut));
    }

    @GetMapping("/candidatures/type/{typeStage}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CandidatureResponse>> listerParTypeStage(@PathVariable TypeStage typeStage) {
        return ResponseEntity.ok(agentRHService.listerCandidaturesParTypeStage(typeStage));
    }

    @GetMapping("/candidatures/type/{typeStage}/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<CandidatureResponse>> listerParTypeEtStatut(
            @PathVariable TypeStage typeStage,
            @PathVariable StatutCandidature statut) {
        return ResponseEntity.ok(agentRHService.listerCandidaturesParTypeEtStatut(typeStage, statut));
    }

    @GetMapping("/candidatures/statistiques")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(agentRHService.getStatistiquesCandidatures());
    }
}


