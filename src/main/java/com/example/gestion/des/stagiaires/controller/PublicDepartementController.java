package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.DepartementResponse;
import com.example.gestion.des.stagiaires.service.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/departements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicDepartementController {

    private final DepartementService departementService;

    @GetMapping
    public ResponseEntity<List<DepartementResponse>> listerDepartements() {
        return ResponseEntity.ok(departementService.listerActifs());
    }
}
