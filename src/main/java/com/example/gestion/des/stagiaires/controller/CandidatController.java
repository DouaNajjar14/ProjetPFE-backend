package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.CandidatResponse;
import com.example.gestion.des.stagiaires.service.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidatController {

    private final CandidatService candidatService;

    @GetMapping
    public ResponseEntity<List<CandidatResponse>> listerTous() {
        return ResponseEntity.ok(candidatService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidatResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(candidatService.trouverParId(id));
    }
}
