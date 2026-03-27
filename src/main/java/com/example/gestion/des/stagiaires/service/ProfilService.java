package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.*;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.exception.ValidationException;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    /**
     * Récupère le profil de l'utilisateur actuellement connecté
     */
    public ProfilUtilisateurResponse obtenirProfilCourant() {
        Utilisateur utilisateur = obtenirUtilisateurCourant();
        return convertToProfilResponse(utilisateur);
    }

    /**
     * Modifie les coordonnées (email et téléphone) de l'utilisateur actuel
     * IMPORTANT: La modification du NOM et PRENOM est interdite
     */
    public ProfilUtilisateurResponse modifierCoordonnees(ModifierCoordonneeRequest request) {
        Utilisateur utilisateur = obtenirUtilisateurCourant();

        // Validation de l'email
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String emailTrim = request.getEmail().trim();
            // Vérifier que l'email n'existe pas déjà pour un autre utilisateur
            utilisateurRepository.findByEmail(emailTrim).ifPresent(u -> {
                if (!u.getId().equals(utilisateur.getId())) {
                    throw new ValidationException("Cet email est déjà utilisé par un autre utilisateur");
                }
            });
            utilisateur.setEmail(emailTrim);
        }

        // Modification du téléphone
        if (request.getTelephone() != null && !request.getTelephone().isBlank()) {
            utilisateur.setTel(request.getTelephone().trim());
        }

        // IMPORTANT: On ne modifie JAMAIS nom et prenom ici
        // Même si le client envoie une requête malveillante avec ces champs,
        // ils ne seront pas utilisés puisqu'ils ne sont pas dans le DTO
        // ModifierCoordonneeRequest

        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        log.info("Coordonnées modifiées pour l'utilisateur: {}", utilisateur.getId());

        return convertToProfilResponse(utilisateur);
    }

    /**
     * Change le mot de passe de l'utilisateur actuel
     * CRITICAL SECURITY:
     * - Vérifie le mot de passe actuel avec passwordEncoder.matches()
     * - Valide le nouveau mot de passe (8 chars + majuscule + minuscule + chiffre +
     * spécial)
     * - Invalide tous les autres RefreshTokens de cet utilisateur (logout forcé des
     * autres sessions)
     */
    public Map<String, Object> changerMotDePasse(ChangerMotDePasseRequest request) {
        Utilisateur utilisateur = obtenirUtilisateurCourant();

        // 1. Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(request.getMotDePasseActuel(), utilisateur.getMotDePasse())) {
            log.warn("Tentative de changement de mot de passe avec ancien mot de passe incorrect pour: {}",
                    utilisateur.getId());
            throw new ValidationException("Le mot de passe actuel est incorrect");
        }

        // 2. Valider le nouveau mot de passe
        String nouveauPass = request.getNouveauMotDePasse();
        validationNouveauMotDePasse(nouveauPass);

        // 3. S'assurer que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(nouveauPass, utilisateur.getMotDePasse())) {
            throw new ValidationException("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
        }

        // 4. Mettre à jour le mot de passe (encodé)
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauPass));
        utilisateur.setDateChangementMotDePasse(LocalDateTime.now());
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        log.info("Mot de passe changé pour l'utilisateur: {}", utilisateur.getId());

        // 5. CRITICAL: Invalider tous les RefreshTokens SAUF le token courant
        // TODO: Implémenter la suppression des refresh tokens via
        // RefreshTokenRepository
        // Cette étape garantit que toutes les autres sessions de l'utilisateur sont
        // déconnectées
        // invalidateAllRefreshTokensExceptCurrent(utilisateur.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message",
                "Mot de passe changé avec succès. Vous êtes maintenant déconnecté des autres appareils.");
        response.put("utilisateur", convertToProfilResponse(utilisateur));

        return response;
    }

    /**
     * Récupère l'activité (audit log) de l'utilisateur actuel
     */
    public List<ActiviteLogResponse> obtenirActivite() {
        Utilisateur utilisateur = obtenirUtilisateurCourant();
        // TODO: Implémenter la récupération depuis une table d'audit
        // Pour l'instant, retourner une liste vide
        return new ArrayList<>();
    }

    /**
     * Récupère les sessions actives (devices) de l'utilisateur actuel
     */
    public List<SessionActiveResponse> obtenirSessionsActives() {
        Utilisateur utilisateur = obtenirUtilisateurCourant();
        // TODO: Implémenter via une table de sessions/devices
        // Pour l'instant, retourner une liste vide
        return new ArrayList<>();
    }

    /**
     * Déconnecte une session spécifique (device/navigateur)
     */
    public void deconnecterSession(Long sessionId) {
        // TODO: Implémenter la suppression de session via RefreshTokenRepository
        // deleteRefreshTokenBySessionId(sessionId);
        log.info("Session {} déconnectée", sessionId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Récupère l'utilisateur actuellement connecté depuis le SecurityContext
     */
    private Utilisateur obtenirUtilisateurCourant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("Utilisateur non authentifié");
        }

        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Utilisateur non trouvé"));
    }

    /**
     * Valide le nouveau mot de passe selon les critères:
     * - Minimum 8 caractères
     * - Au moins une majuscule
     * - Au moins une minuscule
     * - Au moins un chiffre
     * - Au moins un caractère spécial
     */
    private void validationNouveauMotDePasse(String motDePasse) {
        Map<String, Boolean> requirements = new HashMap<>();

        requirements.put("length", motDePasse.length() >= 8);
        requirements.put("uppercase", PASSWORD_UPPERCASE.matcher(motDePasse).find());
        requirements.put("lowercase", PASSWORD_LOWERCASE.matcher(motDePasse).find());
        requirements.put("digit", PASSWORD_DIGIT.matcher(motDePasse).find());
        requirements.put("special", PASSWORD_SPECIAL.matcher(motDePasse).find());

        StringBuilder missingRequirements = new StringBuilder();

        if (!requirements.get("length")) {
            missingRequirements.append("- Au moins 8 caractères\n");
        }
        if (!requirements.get("uppercase")) {
            missingRequirements.append("- Au moins une majuscule\n");
        }
        if (!requirements.get("lowercase")) {
            missingRequirements.append("- Au moins une minuscule\n");
        }
        if (!requirements.get("digit")) {
            missingRequirements.append("- Au moins un chiffre\n");
        }
        if (!requirements.get("special")) {
            missingRequirements.append("- Au moins un caractère spécial\n");
        }

        if (missingRequirements.length() > 0) {
            throw new ValidationException(
                    "Le mot de passe ne respecte pas les critères requis:\n" + missingRequirements);
        }
    }

    /**
     * Convertit une entité Utilisateur en DTO ProfilUtilisateurResponse
     */
    private ProfilUtilisateurResponse convertToProfilResponse(Utilisateur utilisateur) {
        ProfilUtilisateurResponse response = new ProfilUtilisateurResponse();
        // Utiliser l'UUID comme String directement
        response.setId(utilisateur.getId().toString());
        response.setPrenom(utilisateur.getPrenom());
        response.setNom(utilisateur.getNom());
        response.setEmail(utilisateur.getEmail());
        response.setTelephone(utilisateur.getTel());
        response.setRole(utilisateur.getRole().name());
        response.setStatut(utilisateur.getActif() ? "Actif" : "Inactif");
        response.setCreatedAt(utilisateur.getDateCreation().format(DATE_FORMATTER));
        response.setUpdatedAt(utilisateur.getDateModification().format(DATE_FORMATTER));
        return response;
    }

    /**
     * TODO: Implémenter l'invalidation de tous les refresh tokens sauf le courant
     * Cette méthode doit:
     * 1. Récupérer le RefreshToken courant depuis l'Authorization header
     * 2. Supprimer tous les RefreshTokens de cet utilisateur SAUF le courant
     * 3. Assurer que le logout est instantané pour les autres sessions
     */
    // private void invalidateAllRefreshTokensExceptCurrent(UUID userId) {
    // String currentToken = extractTokenFromRequest();
    // refreshTokenRepository.deleteByUtilisateurIdAndTokenNot(userId,
    // currentToken);
    // }
}
