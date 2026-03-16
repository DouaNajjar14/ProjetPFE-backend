package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.LoginRequest;
import com.example.gestion.des.stagiaires.dto.LoginResponse;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
}
