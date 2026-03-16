package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.PendingAccountRequest;
import com.example.gestion.des.stagiaires.dto.PendingAccountResponse;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.entity.Departement;
import com.example.gestion.des.stagiaires.entity.Encadrant;
import com.example.gestion.des.stagiaires.entity.PendingAccount;
import com.example.gestion.des.stagiaires.entity.Stagiaire;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.enums.PendingAccountStatus;
import com.example.gestion.des.stagiaires.enums.Role;
import com.example.gestion.des.stagiaires.enums.StatutStagiaire;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.CandidatureRepository;
import com.example.gestion.des.stagiaires.repository.EncadrantRepository;
import com.example.gestion.des.stagiaires.repository.PendingAccountRepository;
import com.example.gestion.des.stagiaires.repository.StagiaireRepository;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import com.example.gestion.des.stagiaires.repository.DepartementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PendingAccountService {

    private final PendingAccountRepository pendingAccountRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandidatureRepository candidatureRepository;
    private final EncadrantRepository encadrantRepository;
    private final StagiaireRepository stagiaireRepository;
    private final DepartementRepository departementRepository;

    // ─── 1. Création du pending account (appelé par n8n) ───────────────

    @Transactional
    public PendingAccountResponse create(PendingAccountRequest request) {

        // Vérifier qu'il n'y a pas déjà un pending EN_ATTENTE pour cette candidature
        if (pendingAccountRepository.existsByCandidatureIdAndStatut(
                request.getCandidatureId(), PendingAccountStatus.EN_ATTENTE)) {
            throw new IllegalStateException(
                    "Un compte en attente existe déjà pour la candidature " + request.getCandidatureId());
        }

        // Vérifier unicité username
        if (pendingAccountRepository.existsByUsername(request.getUsername())) {
            throw new IllegalStateException("Le username '" + request.getUsername() + "' est déjà utilisé.");
        }

        // Vérifier que l'email n'a pas déjà un compte utilisateur
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("L'email '" + request.getEmail() + "' a déjà un compte utilisateur.");
        }

        String token = UUID.randomUUID().toString();
        String tempPasswordClear = request.getTempPassword();

        PendingAccount entity = PendingAccount.builder()
                .candidatureId(request.getCandidatureId())
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .email(request.getEmail())
                .username(request.getUsername())
                .tempPasswordHash(passwordEncoder.encode(tempPasswordClear))
                .departement(request.getDepartement())
                .encadrantId(request.getEncadrantId())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .typeStage(TypeStage.valueOf(request.getTypeStage()))
                .token(token)
                .statut(PendingAccountStatus.EN_ATTENTE)
                .expiresAt(LocalDateTime.now().plusHours(48))
                .build();

        PendingAccount saved = pendingAccountRepository.save(entity);

        log.info("[PendingAccount] Créé pour {} {} (candidature={}, token={})",
                saved.getPrenom(), saved.getNom(), saved.getCandidatureId(), token);

        // Retourner la réponse avec le mot de passe en clair (une seule fois)
        return toResponse(saved, tempPasswordClear);
    }

    // ─── 2. Consultation par token (admin) ─────────────────────────────

    @Transactional(readOnly = true)
    public PendingAccountResponse getByToken(String token) {
        PendingAccount account = findByTokenOrThrow(token);
        validateAccountStatus(account);
        return toResponse(account, null);
    }

    // ─── 3. Confirmation : création du compte stagiaire ────────────────

    @Transactional
    public PendingAccountResponse confirm(String token) {
        PendingAccount account = findByTokenOrThrow(token);
        validateAccountStatus(account);

        if (stagiaireRepository.existsByCandidatureId(account.getCandidatureId())) {
            throw new TokenAlreadyUsedException("Un stagiaire existe déjà pour cette candidature.");
        }

        Candidature candidature = candidatureRepository.findById(account.getCandidatureId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Candidature introuvable : " + account.getCandidatureId()));

        Candidat candidat = candidature.getCandidat1();

        Encadrant encadrant = null;
        if (account.getEncadrantId() != null) {
            encadrant = encadrantRepository.findById(account.getEncadrantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Encadrant introuvable : " + account.getEncadrantId()));
        }

        if (account.getDateDebut() == null) {
            throw new IllegalStateException("dateDebut est obligatoire pour confirmer le stagiaire.");
        }
        if (account.getDateFin() != null && account.getDateFin().isBefore(account.getDateDebut())) {
            throw new IllegalStateException("dateFin ne peut pas être avant dateDebut.");
        }

        // Résolution souple du département :
        // 1) encadrant.getDepartement() si encadrantId non null
        // 2) sinon lookup par nom si departement non null
        // 3) sinon on laisse null — le RH l'affectera plus tard
        Departement departement = null;
        if (encadrant != null) {
            departement = encadrant.getDepartement();
        } else if (account.getDepartement() != null && !account.getDepartement().isBlank()) {
            departement = departementRepository.findByNom(account.getDepartement()).orElse(null);
        }

        // ── Générer un nouveau mot de passe temporaire en clair ────────────────
        // On génère un mot de passe ici (jamais stocké en base côté PendingAccount).
        // Il sera renvoyé une seule fois dans la réponse pour que n8n l'envoie au stagiaire.
        String newTempPasswordClair = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // Créer le compte utilisateur (stagiaire)
        Utilisateur user = Utilisateur.builder()
                .prenom(account.getPrenom())
                .nom(account.getNom())
                .email(account.getEmail())
                .motDePasse(passwordEncoder.encode(newTempPasswordClair))
                .role(Role.STAGIAIRE)
                .actif(true)
                .build();

        Utilisateur savedUser = utilisateurRepository.save(user);

        // Créer l'entité métier Stagiaire
        Stagiaire stagiaire = Stagiaire.builder()
                .user(savedUser)
                .candidat(candidat)
                .candidature(candidature)
                .prenom(account.getPrenom())
                .nom(account.getNom())
                .email(account.getEmail())
                .telephone(candidat != null ? candidat.getTel() : null)
                .universite(candidat != null ? candidat.getUniversite() : null)
                .typeStage(account.getTypeStage())
                .departement(departement)
                .encadrant(encadrant)
                .dateDebut(account.getDateDebut())
                .dateFin(account.getDateFin())
                .statut(StatutStagiaire.ACTIF)
                .build();

        Stagiaire savedStagiaire = stagiaireRepository.save(stagiaire);

        // Marquer le pending account comme confirmé
        account.setStatut(PendingAccountStatus.CONFIRME);
        account.setStagiaireId(savedUser.getId());
        pendingAccountRepository.save(account);

        log.info("[PendingAccount] Confirmé → utilisateur {} créé (userId={}, stagiaireId={}, email={})",
                savedUser.getPrenom() + " " + savedUser.getNom(),
                savedUser.getId(), savedStagiaire.getId(), savedUser.getEmail());

        return PendingAccountResponse.builder()
                .stagiaireId(savedUser.getId())
                .username(account.getUsername())
                // tempPassword reste null (champ réservé au POST de création)
                .tempPasswordClair(newTempPasswordClair)
                .prenom(account.getPrenom())
                .nom(account.getNom())
                .email(account.getEmail())
                .statut(PendingAccountStatus.CONFIRME)
                .build();
    }

    // ─── 4. Scheduled : expirer les tokens périmés ─────────────────────

    @Scheduled(fixedRate = 3600000) // toutes les heures
    @Transactional
    public void expireOldPendingAccounts() {
        int count = pendingAccountRepository.expireOldTokens(LocalDateTime.now());
        if (count > 0) {
            log.info("[PendingAccount] {} token(s) expiré(s)", count);
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private PendingAccount findByTokenOrThrow(String token) {
        return pendingAccountRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide : " + token));
    }

    private void validateAccountStatus(PendingAccount account) {
        if (account.getStatut() == PendingAccountStatus.EXPIRE
                || account.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Mettre à jour le statut si pas déjà fait
            if (account.getStatut() == PendingAccountStatus.EN_ATTENTE) {
                account.setStatut(PendingAccountStatus.EXPIRE);
                pendingAccountRepository.save(account);
            }
            throw new TokenExpiredException("Ce lien a expiré.");
        }
        if (account.getStatut() == PendingAccountStatus.CONFIRME) {
            throw new TokenAlreadyUsedException("Ce compte a déjà été confirmé.");
        }
    }

    private PendingAccountResponse toResponse(PendingAccount entity, String tempPasswordClear) {
        return PendingAccountResponse.builder()
                .id(entity.getId())
                .candidatureId(entity.getCandidatureId())
                .prenom(entity.getPrenom())
                .nom(entity.getNom())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .departement(entity.getDepartement())
                .encadrantId(entity.getEncadrantId())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .typeStage(entity.getTypeStage().name())
                .token(entity.getToken())
                .statut(entity.getStatut())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .tempPassword(tempPasswordClear)
                .stagiaireId(entity.getStagiaireId())
                .build();
    }

    // ─── Exceptions métier ─────────────────────────────────────────────

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) { super(message); }
    }

    public static class TokenAlreadyUsedException extends RuntimeException {
        public TokenAlreadyUsedException(String message) { super(message); }
    }
}
