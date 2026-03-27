package com.example.gestion.des.stagiaires.service.doublon;

import com.example.gestion.des.stagiaires.dto.CandidatureRequest;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DoublonValidator — Service complète de détection des doublons
 * Implémente les 3 niveaux de détection avec logique métier stricte
 *
 * Niveau 1 — Blocage automatique (aucune intervention RH)
 * - Même email + même type + même année
 * - Initiation + Perfectionnement même année (fenêtres identiques,
 * incompatibles)
 * - PFE + Été même année (PFE dure 4-6 mois depuis fév, overlap au moins
 * partiel)
 * - Initiation + PFE même année (incompatibilité académique : Licence vs
 * Master)
 *
 * Niveau 2 — Avertissement RH (popup orange, RH peut continuer)
 * - Perfectionnement + PFE même année (fin perfectionnement ~7 fév, début PFE
 * 1er fév,
 * dates potentiellement consécutives ou chevauchantes → RH vérifie avec
 * convention)
 *
 * Niveau 3 — Information discrète (aucun blocage)
 * - Même nom + prénom mais email différent (possible homonyme)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoublonValidator {

    private final CandidatureRepository candidatureRepository;

    /**
     * Valide une nouvelle candidature pour détecter les doublons
     *
     * @param candidatureRequest La candidature en cours de création
     * @param candidat1          L'instance du candidat principal
     * @param year               L'année de la candidature (normalement année
     *                           courante)
     * @return DoublonCheckResult avec le niveau détecté et message
     */
    @Transactional(readOnly = true)
    public DoublonCheckResult validateDoublon(CandidatureRequest candidatureRequest,
            Candidat candidat1,
            Integer year) {

        TypeStage typeStageRequest = candidatureRequest.getTypeStage();

        log.info("Validation doublon — email: {}, type: {}, année: {}",
                candidat1.getEmail(), typeStageRequest, year);

        // ── NIVEAU 1 : Blocage automatique ──

        // 1.1 - Même email + même type + même année
        DoublonCheckResult same_email_type_year = checkSameEmailTypeYear(candidat1.getEmail(), typeStageRequest, year);
        if (same_email_type_year.getIsBlocking()) {
            log.warn("Doublon Niveau 1 détecté (même email+type+année): {}", same_email_type_year.getMessage());
            return same_email_type_year;
        }

        // 1.2 - Initiation + Perfectionnement même année (fenêtres identiques,
        // impossible)
        DoublonCheckResult initiation_perfectionnement = checkInitiationPerfectionnementConflict(candidat1.getId(),
                typeStageRequest, year);
        if (initiation_perfectionnement.getIsBlocking()) {
            log.warn("Doublon Niveau 1 détecté (Initiation+Perfectionnement): {}",
                    initiation_perfectionnement.getMessage());
            return initiation_perfectionnement;
        }

        // 1.3 - PFE + Été même année (overlap temporel)
        DoublonCheckResult pfe_summer = checkPFESummerConflict(candidat1.getId(), typeStageRequest, year);
        if (pfe_summer.getIsBlocking()) {
            log.warn("Doublon Niveau 1 détecté (PFE+Été): {}", pfe_summer.getMessage());
            return pfe_summer;
        }

        // 1.4 - Initiation + PFE même année (incompatibilité académique)
        DoublonCheckResult initiation_pfe = checkInitiationPFEConflict(candidat1.getId(), typeStageRequest, year);
        if (initiation_pfe.getIsBlocking()) {
            log.warn("Doublon Niveau 1 détecté (Initiation+PFE): {}", initiation_pfe.getMessage());
            return initiation_pfe;
        }

        // ── NIVEAU 2 : Avertissement RH ──

        // 2.1 - Perfectionnement + PFE même année (dates consécutives possibles)
        DoublonCheckResult perfectionnement_pfe = checkPerfectionnementPFEWarning(candidat1.getId(), typeStageRequest,
                year);
        if (perfectionnement_pfe.getNiveau() == DoublonLevel.NIVEAU_2) {
            log.info("Doublon Niveau 2 (avertissement) détecté (Perfectionnement+PFE): {}",
                    perfectionnement_pfe.getMessage());
            return perfectionnement_pfe;
        }

        // ── NIVEAU 3 : Information discrète ──

        // 3.1 - Même nom + prénom mais email différent (homonyme possible)
        DoublonCheckResult possible_homonym = checkPossibleHomonym(candidat1);
        if (possible_homonym.getNiveau() == DoublonLevel.NIVEAU_3) {
            log.info("Information discrète (homonyme possible): {}", possible_homonym.getMessage());
            return possible_homonym;
        }

        // Aucun doublon détecté
        log.info("Aucun doublon détecté pour email: {}", candidat1.getEmail());
        return DoublonCheckResult.aucun();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NIVEAU 1 — Blocages automatiques
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Vérification 1.1 : Même email + même type + même année
     * Doublon évident → BLOC
     */
    private DoublonCheckResult checkSameEmailTypeYear(String email, TypeStage typeStage, Integer year) {
        List<Candidature> existing = candidatureRepository.findByEmailTypeYearActive(email, typeStage, year);

        if (!existing.isEmpty()) {
            Candidature first = existing.get(0);
            return DoublonCheckResult.niveau1(
                    "Candidature existante pour ce candidat avec ce type de stage cette année",
                    "Candidature ID: " + first.getId(),
                    "Une candidature pour ce type de stage existe déjà cette année (Candidature n° " + first.getId()
                            + "). " +
                            "Vous ne pouvez pas soumettre deux candidatures pour le même type de stage la même année.");
        }
        return DoublonCheckResult.aucun();
    }

    /**
     * Vérification 1.2 : Initiation + Perfectionnement même année
     * Fenêtres identiques au jour près → chevauchement total → BLOC
     * Un candidat Licence (Initiation) ne peut pas être Master (Perfectionnement)
     */
    private DoublonCheckResult checkInitiationPerfectionnementConflict(UUID candidatId,
            TypeStage currentType,
            Integer year) {
        // Si le type courant n'est ni INITIATION ni PERFECTIONNEMENT, pas de conflit
        // possible
        if (currentType != TypeStage.INITIATION && currentType != TypeStage.PERFECTIONNEMENT) {
            return DoublonCheckResult.aucun();
        }

        // Chercher l'autre type
        TypeStage otherType = (currentType == TypeStage.INITIATION) ? TypeStage.PERFECTIONNEMENT : TypeStage.INITIATION;

        List<Candidature> existing = candidatureRepository.findByCandidatAndTypeAndYear(
                candidatId, otherType, year);

        if (!existing.isEmpty()) {
            return DoublonCheckResult.niveau1(
                    "Candidat ne peut pas avoir Initiation ET Perfectionnement la même année (fenêtres identiques)",
                    "Candiat ID: " + candidatId + ", année: " + year,
                    "⚠️  L'Initiation et le Perfectionnement partagent les mêmes dates (7 janvier – 7 février). " +
                            "Vous ne pouvez donc postuler que pour l'un des deux la même année. Choisissez lequel vous préférez.");
        }
        return DoublonCheckResult.aucun();
    }

    /**
     * Vérification 1.3 : PFE + Été même année
     * PFE depuis 1er fév pendant 4-6 mois → dépasse largement le 1er juillet → BLOC
     */
    private DoublonCheckResult checkPFESummerConflict(UUID candidatId, TypeStage currentType, Integer year) {
        if (currentType != TypeStage.PFE && currentType != TypeStage.ETE) {
            return DoublonCheckResult.aucun();
        }

        TypeStage otherType = (currentType == TypeStage.PFE) ? TypeStage.ETE : TypeStage.PFE;

        List<Candidature> existing = candidatureRepository.findByCandidatAndTypeAndYear(
                candidatId, otherType, year);

        if (!existing.isEmpty()) {
            return DoublonCheckResult.niveau1(
                    "Candidat ne peut pas avoir PFE ET Été la même année (overlap temporel)",
                    "Candidat ID: " + candidatId + ", année: " + year,
                    "⏰ Le PFE et la session d'été ne peuvent pas être suivis la même année. " +
                            "Le PFE s'étend de février à juin/juillet, ce qui recouvre entièrement la période de la session d'été. "
                            +
                            "Vous devez choisir l'un de ces deux stages pour l'année actuelle.");
        }
        return DoublonCheckResult.aucun();
    }

    /**
     * Vérification 1.4 : Initiation + PFE même année
     * Initiation = Licence | PFE = Master/Ingénieur → incompatibilité de profil →
     * BLOC
     */
    private DoublonCheckResult checkInitiationPFEConflict(UUID candidatId,
            TypeStage currentType,
            Integer year) {
        if (currentType != TypeStage.INITIATION && currentType != TypeStage.PFE) {
            return DoublonCheckResult.aucun();
        }

        TypeStage otherType = (currentType == TypeStage.INITIATION) ? TypeStage.PFE : TypeStage.INITIATION;

        List<Candidature> existing = candidatureRepository.findByCandidatAndTypeAndYear(
                candidatId, otherType, year);

        if (!existing.isEmpty()) {
            return DoublonCheckResult.niveau1(
                    "Candidat ne peut pas avoir Initiation ET PFE la même année (incompatibilité académique)",
                    "Candidat ID: " + candidatId + ", année: " + year,
                    "❌ Ces deux stages ne sont pas compatibles. L'Initiation est pour les étudiants en Licence, " +
                            "tandis que le PFE est réservé aux niveaux Master et Ingénieur. " +
                            "Vous ne pouvez postuler que pour un seul de ces deux stages la même année.");
        }
        return DoublonCheckResult.aucun();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NIVEAU 2 — Avertissements RH
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Vérification 2.1 : Perfectionnement + PFE même année
     * Dates potentiellement consécutives (fin Perfectionnement ~7 fév, début PFE
     * 1er fév)
     * → AVERTISSEMENT au RH (peut continuer en cochant)
     */
    private DoublonCheckResult checkPerfectionnementPFEWarning(UUID candidatId,
            TypeStage currentType,
            Integer year) {
        if (currentType != TypeStage.PERFECTIONNEMENT && currentType != TypeStage.PFE) {
            return DoublonCheckResult.aucun();
        }

        TypeStage otherType = (currentType == TypeStage.PERFECTIONNEMENT) ? TypeStage.PFE : TypeStage.PERFECTIONNEMENT;

        List<Candidature> existing = candidatureRepository.findByCandidatAndTypeAndYear(
                candidatId, otherType, year);

        if (!existing.isEmpty()) {
            return DoublonCheckResult.niveau2(
                    "Avertissement : Perfectionnement + PFE même année (dates potentiellement consécutives)",
                    "Candidat ID: " + candidatId + ", année: " + year,
                    "⚠️ Attention : Vous avez déjà une candidature pour " +
                            (currentType == TypeStage.PERFECTIONNEMENT ? "le PFE" : "le Perfectionnement") +
                            " la même année. " +
                            "Le Perfectionnement se termine vers le 7 février, le PFE commence le 1er février. " +
                            "Les dates peuvent être consécutives ou chevauchantes. " +
                            "Êtes-vous sûr de vouloir continuer ? Consultez votre convention de stage.");
        }
        return DoublonCheckResult.aucun();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NIVEAU 3 — Informations discrètes
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Vérification 3.1 : Même nom + prénom mais email différent
     * Possible homonyme → INFO discrète au RH (aucun blocage)
     */
    private DoublonCheckResult checkPossibleHomonym(Candidat candidat) {
        List<Candidature> existing = candidatureRepository.findByNomPrenomButDifferentEmail(
                candidat.getNom(), candidat.getPrenom(), candidat.getEmail());

        if (!existing.isEmpty()) {
            Candidature first = existing.get(0);
            return DoublonCheckResult.niveau3(
                    "Information : possible homonyme détecté (même nom+prénom, email différent)",
                    "Candidature homonyme ID: " + first.getId() + ", email: " + first.getCandidat1().getEmail(),
                    "ℹ️ Information : Un candidat portant le même nom et prénom a été détecté dans le système " +
                            "mais avec une adresse e-mail différente. Assurez-vous qu'il ne s'agit pas d'un doublon.");
        }
        return DoublonCheckResult.aucun();
    }
}
