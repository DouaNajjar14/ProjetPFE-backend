package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.PendingAccountResponse;
import com.example.gestion.des.stagiaires.service.PendingAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint public (sans JWT) pour confirmer un pending account
 * en one-click depuis un lien dans un email.
 * Redirige vers la plateforme après succès.
 */
@RestController
@RequestMapping("/api/admin/confirm-direct")
@RequiredArgsConstructor
@Slf4j
public class ConfirmDirectController {

    private final PendingAccountService pendingAccountService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * GET /api/admin/confirm-direct/{token}
     * Confirme le pending account et redirige vers le front.
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> confirmDirect(@PathVariable String token) {
        try {
            PendingAccountResponse response = pendingAccountService.confirm(token);
            log.info("[ConfirmDirect] Compte stagiaire créé pour {} {} (id={})",
                    response.getPrenom(), response.getNom(), response.getStagiaireId());

            // Redirection vers la plateforme avec message de succès
            String redirectUrl = frontendUrl + "/admin/stagiaires?account-confirmed=true&stagiaireId=" + response.getStagiaireId();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } catch (PendingAccountService.TokenExpiredException e) {
            String redirectUrl = frontendUrl + "/login?error=token-expired";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } catch (PendingAccountService.TokenAlreadyUsedException e) {
            String redirectUrl = frontendUrl + "/login?error=already-confirmed";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Token invalide."));
        }
    }
}

