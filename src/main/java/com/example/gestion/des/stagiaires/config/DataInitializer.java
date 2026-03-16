package com.example.gestion.des.stagiaires.config;

import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.enums.Role;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer un admin par défaut si aucun n'existe
        if (!utilisateurRepository.existsByEmail("admin@ooredoo.tn")) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Admin")
                    .prenom("System")
                    .email("admin@ooredoo.tn")
                    .motDePasse(passwordEncoder.encode("Admin123@"))
                    .tel("00000000")
                    .role(Role.ADMIN)
                    .actif(true)
                    .build();

            utilisateurRepository.save(admin);
            log.info("Admin par défaut créé : admin@ooredoo.tn / Admin123@");
        }
    }
}
