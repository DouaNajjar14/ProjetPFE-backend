package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.SujetPfeResponse;
import com.example.gestion.des.stagiaires.service.SujetPfeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/sujets-pfe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicSujetPfeController {

    private final SujetPfeService sujetPfeService;

    @GetMapping
    public ResponseEntity<List<SujetPfeResponse>> listerSujetsDisponibles() {
        return ResponseEntity.ok(sujetPfeService.listerSujetsOuverts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SujetPfeResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(sujetPfeService.trouverParId(id));
    }

    @GetMapping("/departement/{departementId}")
    public ResponseEntity<List<SujetPfeResponse>> listerParDepartement(@PathVariable UUID departementId) {
        return ResponseEntity.ok(sujetPfeService.listerParDepartement(departementId));
    }
}
