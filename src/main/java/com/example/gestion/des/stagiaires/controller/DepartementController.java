package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.DepartementRequest;
import com.example.gestion.des.stagiaires.dto.DepartementResponse;
import com.example.gestion.des.stagiaires.service.DepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/departements")
@RequiredArgsConstructor

public class DepartementController {

    private final DepartementService departementService;

    @PostMapping
    public ResponseEntity<DepartementResponse> creer(@Valid @RequestBody DepartementRequest request) {
        DepartementResponse response = departementService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartementResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody DepartementRequest request) {
        DepartementResponse response = departementService.modifier(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/archiver")
    public ResponseEntity<DepartementResponse> archiver(@PathVariable UUID id) {
        DepartementResponse response = departementService.archiver(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desarchiver")
    public ResponseEntity<DepartementResponse> desarchiver(@PathVariable UUID id) {
        DepartementResponse response = departementService.desarchiver(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DepartementResponse>> listerActifs() {
        return ResponseEntity.ok(departementService.listerActifs());
    }

    @GetMapping("/archives")
    public ResponseEntity<List<DepartementResponse>> listerArchives() {
        return ResponseEntity.ok(departementService.listerArchives());
    }

    @GetMapping("/tous")
    public ResponseEntity<List<DepartementResponse>> listerTous() {
        return ResponseEntity.ok(departementService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartementResponse> trouverParId(@PathVariable UUID id) {
        return ResponseEntity.ok(departementService.trouverParId(id));
    }
}

