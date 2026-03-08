package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.AgentRHRequest;
import com.example.gestion.des.stagiaires.dto.AgentRHResponse;
import com.example.gestion.des.stagiaires.dto.AgentRHUpdateRequest;
import com.example.gestion.des.stagiaires.service.AgentRHService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/agents-rh")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AgentRHController {

    private final AgentRHService agentRHService;

    @PostMapping
    public ResponseEntity<AgentRHResponse> creer(@Valid @RequestBody AgentRHRequest request) {
        AgentRHResponse response = agentRHService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentRHResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody AgentRHUpdateRequest request) {
        AgentRHResponse response = agentRHService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archiver")
    public ResponseEntity<AgentRHResponse> archiver(@PathVariable UUID id) {
        AgentRHResponse response = agentRHService.archiver(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desarchiver")
    public ResponseEntity<AgentRHResponse> desarchiver(@PathVariable UUID id) {
        AgentRHResponse response = agentRHService.desarchiver(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AgentRHResponse>> listerActifs() {
        return ResponseEntity.ok(agentRHService.listerActifs());
    }

    @GetMapping("/archives")
    public ResponseEntity<List<AgentRHResponse>> listerArchives() {
        return ResponseEntity.ok(agentRHService.listerArchives());
    }

    @GetMapping("/tous")
    public ResponseEntity<List<AgentRHResponse>> listerTous() {
        return ResponseEntity.ok(agentRHService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentRHResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(agentRHService.trouverParId(id));
    }
}

