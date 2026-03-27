package com.example.gestion.des.stagiaires.controller;

import com.example.gestion.des.stagiaires.dto.*;
import com.example.gestion.des.stagiaires.service.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT_RH')")
public class ProfilController {

    private final ProfilService profilService;

    /**
     * GET /api/profil/moi
     * Récupère le profil de l'utilisateur actuellement connecté
     */
    @GetMapping("/moi")
    public ResponseEntity<ProfilUtilisateurResponse> obtenirProfilCourant() {
        return ResponseEntity.ok(profilService.obtenirProfilCourant());
    }

    /**
     * PUT /api/profil/coordonnees
     * Modifie les coordonnées (email et téléphone) de l'utilisateur actuel
     * 
     * IMPORTANT SECURITY:
     * - Les champs 'nom' et 'prenom' ne sont pas dans ModifierCoordonneeRequest
     * - Le backend refuse toute tentative de modification de ces champs
     * - Si le client envoie des paramètres supplémentaires, ils sont ignorés
     */
    @PutMapping("/coordonnees")
    public ResponseEntity<ProfilUtilisateurResponse> modifierCoordonnees(
            @Valid @RequestBody ModifierCoordonneeRequest request) {
        return ResponseEntity.ok(profilService.modifierCoordonnees(request));
    }

    /**
     * POST /api/profil/changer-mot-de-passe
     * Change le mot de passe de l'utilisateur actuel
     * 
     * CRITICAL SECURITY:
     * 1. Le mot de passe actuel est vérifié avec passwordEncoder.matches()
     * 2. Le nouveau mot de passe doit contenir:
     * - Au minimum 8 caractères
     * - Au moins une majuscule
     * - Au moins une minuscule
     * - Au moins un chiffre
     * - Au moins un caractère spécial
     * 3. Tous les RefreshTokens de cet utilisateur (sauf le courant) sont supprimés
     * → Ceci force la déconnexion immédiate des autres sessions
     * 
     * @param request Contient motDePasseActuel et nouveauMotDePasse
     * @return Message de succès et nouveau profil utilisateur
     */
    @PostMapping("/changer-mot-de-passe")
    public ResponseEntity<Map<String, Object>> changerMotDePasse(
            @Valid @RequestBody ChangerMotDePasseRequest request) {
        return ResponseEntity.ok(profilService.changerMotDePasse(request));
    }

    /**
     * GET /api/profil/activite
     * Récupère le journal d'activité (audit log) de l'utilisateur actuel
     * 
     * Retourne les événements suivants:
     * - Connexions
     * - Modifications de profil
     * - Changements de mot de passe
     * - Déconnexions
     */
    @GetMapping("/activite")
    public ResponseEntity<List<ActiviteLogResponse>> obtenirActivite() {
        return ResponseEntity.ok(profilService.obtenirActivite());
    }

    /**
     * GET /api/profil/sessions
     * Récupère les sessions actives (devices/navigateurs) de l'utilisateur actuel
     * 
     * Retourne pour chaque session:
     * - Type d'appareil (Windows, MacOS, Linux, iPhone, Android, etc.)
     * - Navigateur utilisé (Chrome, Firefox, Safari, Edge, etc.)
     * - Ville (déduite de l'adresse IP)
     * - État de la session (Actif/Inactif)
     * - Date de création
     * - Dernier accès
     * - Booléen: si c'est la session actuelle
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionActiveResponse>> obtenirSessionsActives() {
        return ResponseEntity.ok(profilService.obtenirSessionsActives());
    }

    /**
     * POST /api/profil/sessions/{sessionId}/deconnecter
     * Déconnecte une session spécifique (device/navigateur) de l'utilisateur actuel
     * 
     * Ceci force la déconnexion de l'appareil/navigateur spécifié.
     * L'utilisateur sera automatiquement redirigé vers la page de connexion
     * après le prochain refresh de sa page.
     * 
     * @param sessionId ID de la session à déconnecter
     */
    @PostMapping("/sessions/{sessionId}/deconnecter")
    public ResponseEntity<Map<String, String>> deconnecterSession(
            @PathVariable Long sessionId) {
        profilService.deconnecterSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "Session déconnectée avec succès",
                "sessionId", sessionId.toString()));
    }
}
