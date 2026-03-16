package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.PendingAccountResponse;
import com.example.gestion.des.stagiaires.service.PendingAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints admin pour consulter et confirmer les pending accounts.
 * Sécurisés par JWT (rôle ADMIN).
 */
@RestController
@RequestMapping("/api/admin/pending-accounts")
@RequiredArgsConstructor
public class PendingAccountAdminController {

    private final PendingAccountService pendingAccountService;

    /**
     * GET /api/admin/pending-accounts/{token}
     * Consulter les données d'un pending account.
     * 410 si expiré, 409 si déjà utilisé.
     */
    @GetMapping("/{token}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getByToken(@PathVariable String token) {
        try {
            PendingAccountResponse response = pendingAccountService.getByToken(token);
            return ResponseEntity.ok(response);
        } catch (PendingAccountService.TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", e.getMessage()));
        } catch (PendingAccountService.TokenAlreadyUsedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/pending-accounts/{token}/confirm
     * Crée le compte stagiaire et passe le statut à CONFIRME.
     */
    @PutMapping("/{token}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirm(@PathVariable String token) {
        try {
            PendingAccountResponse response = pendingAccountService.confirm(token);
            return ResponseEntity.ok(response);
        } catch (PendingAccountService.TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", e.getMessage()));
        } catch (PendingAccountService.TokenAlreadyUsedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

