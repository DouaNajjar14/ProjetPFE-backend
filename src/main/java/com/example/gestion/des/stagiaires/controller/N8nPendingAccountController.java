package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.PendingAccountRequest;
import com.example.gestion.des.stagiaires.dto.PendingAccountResponse;
import com.example.gestion.des.stagiaires.service.PendingAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint appelé par n8n après l'envoi de l'email d'acceptation.
 * Sécurisé par header X-N8N-Token (pas de JWT).
 */
@RestController
@RequestMapping("/api/n8n")
@RequiredArgsConstructor
@Slf4j
public class N8nPendingAccountController {

    private final PendingAccountService pendingAccountService;

    @Value("${app.n8n.api-token:}")
    private String expectedToken;

    @PostMapping("/pending-accounts")
    public ResponseEntity<?> createPendingAccount(
            @RequestHeader(value = "X-N8N-Token", required = false) String token,
            @RequestBody PendingAccountRequest request) {

        // ── Vérification du token n8n ──
        if (expectedToken == null || expectedToken.isBlank()) {
            log.warn("[n8n] app.n8n.api-token non configuré, rejet de la requête.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token n8n non configuré côté serveur."));
        }
        if (token == null || !token.equals(expectedToken)) {
            log.warn("[n8n] Token invalide reçu : {}", token);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token n8n invalide."));
        }

        try {
            PendingAccountResponse response = pendingAccountService.create(request);
            log.info("[n8n] PendingAccount créé pour {} (token={})",
                    request.getEmail(), response.getToken());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

