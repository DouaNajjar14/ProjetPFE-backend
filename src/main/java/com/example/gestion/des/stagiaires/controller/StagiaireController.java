package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.StagiaireResponse;
import com.example.gestion.des.stagiaires.service.StagiaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stagiaires")
@RequiredArgsConstructor
public class StagiaireController {

    private final StagiaireService stagiaireService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<List<StagiaireResponse>> listerTous() {
        return ResponseEntity.ok(stagiaireService.listerTous());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
    public ResponseEntity<StagiaireResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(stagiaireService.trouverParId(id));
    }
}

