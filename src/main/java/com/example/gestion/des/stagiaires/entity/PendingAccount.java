package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.PendingAccountStatus;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pending_accounts")
public class PendingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* ── Lien candidature ── */
    @Column(nullable = false)
    private UUID candidatureId;

    /* ── Infos candidat ── */
    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String email;

    /* ── Credentials ── */
    @Column(nullable = false, unique = true)
    private String username;

    /** Mot de passe temporaire hashé bcrypt */
    @Column(nullable = false)
    private String tempPasswordHash;

    /* ── Affectation stage ── */
    @Column(nullable = false)
    private String departement;

    private UUID encadrantId;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeStage typeStage;

    /* ── Token & statut ── */
    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PendingAccountStatus statut = PendingAccountStatus.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** ID de l'utilisateur créé après confirmation */
    private UUID stagiaireId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

