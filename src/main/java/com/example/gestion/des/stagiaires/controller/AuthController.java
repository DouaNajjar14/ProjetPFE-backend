package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.LoginRequest;
import com.example.gestion.des.stagiaires.dto.LoginResponse;
import com.example.gestion.des.stagiaires.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }
}
