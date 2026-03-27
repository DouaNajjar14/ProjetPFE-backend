package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.ChangePasswordRequest;
import com.example.gestion.des.stagiaires.dto.ChangePasswordResponse;
import com.example.gestion.des.stagiaires.dto.LoginRequest;
import com.example.gestion.des.stagiaires.dto.LoginResponse;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

        if (!utilisateur.getActif()) {
            throw new BadCredentialsException("Compte désactivé");
        }

        String accessToken = jwtService.generateToken(utilisateur);
        String refreshToken = jwtService.generateRefreshToken(utilisateur);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .premierLogin(utilisateur.getPremierLogin())
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

        if (!jwtService.isTokenValid(refreshToken, utilisateur)) {
            throw new BadCredentialsException("Refresh token invalide");
        }

        String newAccessToken = jwtService.generateToken(utilisateur);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .build();
    }

    public ChangePasswordResponse changePasswordFirstLogin(ChangePasswordRequest request) {
        // Récupérer l'utilisateur courant depuis SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

        // Valider les mots de passe
        if (!request.getNouveauMotDePasse().equals(request.getConfirmerMotDePasse())) {
            throw new BadCredentialsException("Les mots de passe ne correspondent pas");
        }

        if (request.getNouveauMotDePasse().length() < 8) {
            throw new BadCredentialsException("Le mot de passe doit contenir au minimum 8 caractères");
        }

        // Encoder et mettre à jour le mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setPremierLogin(false);
        utilisateur.setDateChangementMotDePasse(LocalDateTime.now());

        utilisateurRepository.save(utilisateur);

        // Regénérer un token
        String newAccessToken = jwtService.generateToken(utilisateur);
        String newRefreshToken = jwtService.generateRefreshToken(utilisateur);

        return ChangePasswordResponse.builder()
                .success(true)
                .message("Mot de passe changé avec succès")
                .token(newAccessToken)
                .build();
    }
}
