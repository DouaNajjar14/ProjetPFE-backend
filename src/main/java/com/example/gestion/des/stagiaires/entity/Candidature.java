package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Candidature — Entité principale pour les demandes de stage
 * Workflow complet : Phase 1 (Candidature) → Phase 2 (Documents) → Phase 3
 * (Affectation)
 *
 * Attributs divisés en 5 groupes :
 * 1. Identité et type
 * 2. Session (calculés automatiquement)
 * 3. Statut et flags
 * 4. Dates (dépôt, entretien, traitement, stage)
 * 5. Relations (candidats, sujets PFE, département, encadrant)
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "candidatures")
public class Candidature {

    // ═══════════════════════════════════════════════════════════════════
    // 1. IDENTITÉ ET TYPE
    // ═══════════════════════════════════════════════════════════════════

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    /**
     * Type de stage demandé (INITIATION | PERFECTIONNEMENT | PFE | ETE)
     * Détermine les validations, le profil academic requis et les règles de doublon
     */
    @Column(name = "type_stage", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeStage typeStage;

    // ═══════════════════════════════════════════════════════════════════
    // 2. SESSION - Calculées automatiquement au @PrePersist
    // ═══════════════════════════════════════════════════════════════════

    /**
     * SessionType dérivé du typeStage (HIVER | PFE | ETE)
     * Règle : INITIATION/PERFECTIONNEMENT → HIVER | PFE → PFE | ETE → ETE
     */
    @Column(name = "session", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionType session = null;

    /**
     * Année civile (ex: 2026)
     * Utilisée pour les règles de détection de doublets (même année = conflits
     * possibles)
     * Calculée : LocalDate.now().getYear()
     */
    @Column(name = "annee", nullable = false)
    @Builder.Default
    private Integer annee = null;

    /**
     * Année académique (ex: "2025-2026")
     * Utilisée pour les rapports et statistiques
     * Règle : mois >= 9 → "année-annee+1" | sinon → "(année-1)-année"
     */
    @Column(name = "annee_academique", length = 20, nullable = false)
    @Builder.Default
    private String anneeAcademique = null;

    // ═══════════════════════════════════════════════════════════════════
    // 3. STATUT ET FLAGS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Statut de la candidature dans le workflow
     * Valeurs : EN_ATTENTE → PRESELECTIONNE → ENTRETIEN → ACCEPTE/REFUSE
     * Valeur par défaut : EN_ATTENTE
     */
    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    /**
     * Indique si la candidature est déposée en binôme (deux candidats)
     * Applicable surtout pour les PFE
     * Valeur par défaut : false
     */
    @Column(name = "est_binome", nullable = false)
    @Builder.Default
    private Boolean estBinome = false;

    /**
     * Flag pour avertissement doublon accepté
     * Niveau 2 : Perfectionnement + PFE même année → RH doit reconnaître le risque
     * Si true : RH a volontairement ignoré l'avertissement
     * Valeur par défaut : false
     */
    @Column(name = "avertissement_doublon_accepte", nullable = false)
    @Builder.Default
    private Boolean avertissementDoublonAccepte = false;

    // ═══════════════════════════════════════════════════════════════════
    // 4. DATES - Timeline du workflow
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Date et heure de soumission de la candidature
     * Renseignée automatiquement via @PrePersist
     * Non modifiable (updatable = false)
     */
    @Column(name = "date_depot", nullable = false, updatable = false)
    private LocalDateTime dateDepot;

    /**
     * Date et heure de l'entretien planifié
     * Renseignée par le RH lors du passage en ENTRETIEN
     * Nullable tant qu'aucun entretien n'est programmé
     */
    @Column(name = "date_entretien", nullable = true)
    private LocalDateTime dateEntretien;

    /**
     * Date et heure de la décision finale du RH (acceptation ou refus)
     * Renseignée quand statut passe à ACCEPTE ou REFUSE
     * Nullable tant qu'aucune décision n'est prise
     */
    @Column(name = "date_traitement", nullable = true)
    private LocalDateTime dateTraitement;

    /**
     * Date de début du stage (LOCAL DATE sans time)
     * Renseignée par le RH après validation de la convention
     * Nullable jusqu'à la finalisation
     * Calculée à partir de la convention déposée et du calendrier SessionConfig
     */
    @Column(name = "date_debut", nullable = true)
    private LocalDate dateDebut;

    // ═══════════════════════════════════════════════════════════════════
    // 5. PHASE 2 - Documents et Affectation
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Statut de validation des documents (CV + Lettre de motivation)
     * Valeurs : NON_DEPOSE → EN_ATTENTE_VERIF → VALIDE/REJETE
     * Valeur par défaut : NON_DEPOSE
     * Condition requise : conventionValidee = true avant de remplir
     * dateDebut/dateFin
     */
    @Column(name = "statut_documents", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDocuments statutDocuments = StatutDocuments.NON_DEPOSE;

    /**
     * Flag activation Phase 2
     * true : RH a validé la convention → dateDebut/dateFin peuvent être remplis
     * false : convention en attente
     * Valeur par défaut : false
     */
    @Column(name = "convention_validee", nullable = false)
    @Builder.Default
    private Boolean conventionValidee = false;

    /**
     * Statut global du stage (suivi Phase 3)
     * Valeurs : EN_COURS | TERMINE | ABANDONNE
     * Renseigné après l'affectation à un encadrant
     * Nullable tant qu'aucun stage n'a commencé
     */
    @Column(name = "statut_global", nullable = true)
    @Enumerated(EnumType.STRING)
    private StatutGlobal statutGlobal;

    /**
     * Date d'affectation — quand RH finalise département + encadrant
     * Renseignée quand fase 3 commence (assignation à un encadrant)
     * Nullable tant qu'aucune affectation n'est effectuée
     */
    @Column(name = "date_affectation", nullable = true)
    private LocalDateTime dateAffectation;

    // ═══════════════════════════════════════════════════════════════════
    // 6. RELATIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Candidat principal (obligatoire)
     * Chargement LAZY pour optimiser les performances
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat1_id", nullable = false)
    private Candidat candidat1;

    /**
     * Candidat binôme (optionnel)
     * Renseigné uniquement si estBinome = true
     * Chargement LAZY
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat2_id", nullable = true)
    private Candidat candidat2;

    /**
     * Premier choix de sujet PFE (obligatoire si typeStage = PFE)
     * Relation ManyToOne vers SujetPfe
     * Nullable pour les stages non-PFE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sujet_choix1_id", nullable = true)
    private SujetPfe sujetChoix1;

    /**
     * Deuxième choix de sujet PFE (optionnel)
     * Permet au candidat d'exprimer une préférence secondaire
     * Doit être différent du premier choix
     * Nullable
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sujet_choix2_id", nullable = true)
    private SujetPfe sujetChoix2;

    /**
     * Département affecté (rempli lors de la finalisation Phase 3)
     * Nullable jusqu'à l'affectation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = true)
    private Departement departement;

    /**
     * Encadrant assigné (rempli lors de la finalisation Phase 3)
     * Relation ManyToOne : plusieurs candidatures peuvent être encadrées par le
     * même encadrant
     * Filtré par capacité disponible (capaciteActuelle < capaciteMax)
     * Nullable jusqu'à l'assaffectation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encadrant_id", nullable = true)
    private Encadrant encadrant;

    // ═══════════════════════════════════════════════════════════════════
    // CALCULS AUTOMATIQUES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * @PrePersist — Appelé automatiquement avant chaque INSERT
     *             Calcule les champs automatiques :
     *             - dateDepot = LocalDateTime.now()
     *             - session = dérivée du typeStage (via SessionConfigService)
     *             - annee = année civile
     *             - anneeAcademique = calculée selon le mois courant
     */
    @PrePersist
    protected void onCreate() {
        dateDepot = LocalDateTime.now();
        annee = LocalDate.now().getYear();

        // Calcul anneeAcademique : mois >= 9 → "année-(année+1)" sinon
        // "(année-1)-année"
        int month = LocalDate.now().getMonthValue();
        if (month >= 9) {
            anneeAcademique = annee + "-" + (annee + 1);
        } else {
            anneeAcademique = (annee - 1) + "-" + annee;
        }

        // session sera calculée par le service DoublonValidator
        // avant cet appel @PrePersist à l'aide de SessionConfigService
    }

    /**
     * Utilitaire : Vérifie si la candidature est admissible pour les stages
     * incompatibles
     */
    public boolean isLicenceProfile() {
        return typeStage == TypeStage.INITIATION;
    }

    public boolean isMasterProfile() {
        return typeStage == TypeStage.PERFECTIONNEMENT ||
                typeStage == TypeStage.PFE;
    }

    public boolean isAnySummer() {
        return typeStage == TypeStage.ETE;
    }
}
