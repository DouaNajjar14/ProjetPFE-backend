package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.EncadrantRequest;
import com.example.gestion.des.stagiaires.dto.EncadrantResponse;
import com.example.gestion.des.stagiaires.dto.EncadrantUpdateRequest;
import com.example.gestion.des.stagiaires.service.EncadrantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/encadrants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EncadrantController {

    private final EncadrantService encadrantService;

    @PostMapping
    public ResponseEntity<EncadrantResponse> creer(@Valid @RequestBody EncadrantRequest request) {
        EncadrantResponse response = encadrantService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EncadrantResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody EncadrantUpdateRequest request) {
        EncadrantResponse response = encadrantService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archiver")
    public ResponseEntity<EncadrantResponse> archiver(@PathVariable UUID id) {
        EncadrantResponse response = encadrantService.archiver(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desarchiver")
    public ResponseEntity<EncadrantResponse> desarchiver(@PathVariable UUID id) {
        EncadrantResponse response = encadrantService.desarchiver(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EncadrantResponse>> listerActifs() {
        return ResponseEntity.ok(encadrantService.listerActifs());
    }

    @GetMapping("/archives")
    public ResponseEntity<List<EncadrantResponse>> listerArchives() {
        return ResponseEntity.ok(encadrantService.listerArchives());
    }

    @GetMapping("/tous")
    public ResponseEntity<List<EncadrantResponse>> listerTous() {
        return ResponseEntity.ok(encadrantService.listerTous());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<EncadrantResponse>> listerDisponibles() {
        return ResponseEntity.ok(encadrantService.listerDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EncadrantResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(encadrantService.trouverParId(id));
    }
}
