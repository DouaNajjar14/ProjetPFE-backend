package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.UniversiteRequest;
import com.example.gestion.des.stagiaires.dto.UniversiteResponse;
import com.example.gestion.des.stagiaires.service.UniversiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/universites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UniversiteController {

    private final UniversiteService universiteService;

    @PostMapping
    public ResponseEntity<UniversiteResponse> creer(@Valid @RequestBody UniversiteRequest request) {
        UniversiteResponse response = universiteService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniversiteResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody UniversiteRequest request) {
        UniversiteResponse response = universiteService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
        universiteService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UniversiteResponse>> listerTous() {
        return ResponseEntity.ok(universiteService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversiteResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(universiteService.trouverParId(id));
    }
}
