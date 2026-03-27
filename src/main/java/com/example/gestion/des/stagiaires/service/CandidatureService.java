package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.*;
import com.example.gestion.des.stagiaires.entity.Candidat;
import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.entity.SpecialiteUniversitaire;
import com.example.gestion.des.stagiaires.entity.SujetPfe;
import com.example.gestion.des.stagiaires.enums.SessionType;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.StatutDocuments;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.CandidatureRepository;
import com.example.gestion.des.stagiaires.repository.SujetPfeRepository;
import com.example.gestion.des.stagiaires.service.doublon.DoublonCheckResult;
import com.example.gestion.des.stagiaires.service.doublon.DoublonLevel;
import com.example.gestion.des.stagiaires.service.doublon.DoublonValidator;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final CandidatService candidatService;
    private final SujetPfeRepository sujetPfeRepository;
    private final SessionConfigService sessionConfigService;
    private final DoublonValidator doublonValidator;
    private final RestTemplate restTemplate;

    @Value("${app.n8n.enabled:false}")
    private boolean n8nEnabled;

    @Value("${app.n8n.webhook.confirmation-candidature:}")
    private String webhookConfirmationUrl;

    @Value("${app.n8n.webhook.statut-candidature:}")
    private String webhookStatutUrl;

    @Transactional
    public CandidatureResponse creer(CandidatureRequest request,
            MultipartFile cv1, MultipartFile lettreMotivation1,
            MultipartFile cv2, MultipartFile lettreMotivation2) throws Exception {

        // Validation des contraintes de base
        validateCandidature(request);

        // Créer ou trouver le candidat principal
        Candidat candidat1 = candidatService.creerOuTrouver(request.getCandidat1(), cv1, lettreMotivation1);

        // ── PHASE CRITIQUE : Détection des doublons ──
        Integer currentYear = LocalDate.now().getYear();
        DoublonCheckResult doublonResult = doublonValidator.validateDoublon(
                request,
                candidat1,
                currentYear);

        // Niveau 1 : BLOC IMMÉDIAT
        if (doublonResult.getNiveau() == DoublonLevel.NIVEAU_1) {
            log.error("Doublon Niveau 1 — Blocage immédiat : {}", doublonResult.getMessage());
            throw new IllegalArgumentException(doublonResult.getUserMessage());
        }

        // Niveau 2 : AVERTISSEMENT au RH (mais autorise pour la soumission)
        if (doublonResult.getNiveau() == DoublonLevel.NIVEAU_2) {
            log.warn("Doublon Niveau 2 (avertissement) détecté : {}", doublonResult.getMessage());
            // On peut laisser passer mais on marque un flag pour que le frontend affiche
            // une alerte
            // Ce flag sera rempli côté REST en passant le résultat
        }

        // Niveau 3 : INFO discrète (on continue silencieusement)
        if (doublonResult.getNiveau() == DoublonLevel.NIVEAU_3) {
            log.info("Doublon Niveau 3 (info discrète) détecté : {}", doublonResult.getMessage());
        }

        // Créer ou trouver le candidat binôme si nécessaire
        Candidat candidat2 = null;
        if (Boolean.TRUE.equals(request.getEstBinome()) && request.getCandidat2() != null) {
            candidat2 = candidatService.creerOuTrouver(request.getCandidat2(), cv2, lettreMotivation2);
        }

        // Récupérer les sujets PFE si nécessaire
        SujetPfe sujetChoix1 = null;
        SujetPfe sujetChoix2 = null;
        if (request.getTypeStage() == TypeStage.PFE) {
            if (request.getSujetChoix1Id() != null) {
                sujetChoix1 = sujetPfeRepository.findById(request.getSujetChoix1Id())
                        .orElseThrow(() -> new RuntimeException("Sujet PFE choix 1 non trouvé"));
            }
            if (request.getSujetChoix2Id() != null) {
                sujetChoix2 = sujetPfeRepository.findById(request.getSujetChoix2Id())
                        .orElseThrow(() -> new RuntimeException("Sujet PFE choix 2 non trouvé"));
            }
        }

        // ── Calcul de la session dérivée du typeStage ──
        SessionType sessionType = sessionConfigService.deriveSessionType(request.getTypeStage());

        // Créer la candidature
        Candidature candidature = Candidature.builder()
                .typeStage(request.getTypeStage())
                .session(sessionType)
                .statut(StatutCandidature.EN_ATTENTE)
                .estBinome(request.getEstBinome())
                .candidat1(candidat1)
                .candidat2(candidat2)
                .sujetChoix1(sujetChoix1)
                .sujetChoix2(sujetChoix2)
                .statutDocuments(StatutDocuments.NON_DEPOSE)
                .avertissementDoublonAccepte(doublonResult.getNiveau() == DoublonLevel.NIVEAU_2)
                .build();

        // @PrePersist sera appelé automatiquement pour calculer dateDepot, annee,
        // anneeAcademique
        Candidature saved = candidatureRepository.save(candidature);

        // Notifier n8n — envoi email de confirmation
        envoyerWebhookConfirmation(saved);

        log.info("Candidature créée avec succès : {}", saved.getId());
        return toResponse(saved);
    }

    private void validateCandidature(CandidatureRequest request) {
        if (request.getTypeStage() == TypeStage.PFE) {
            if (request.getSujetChoix1Id() == null) {
                throw new RuntimeException("Le choix d'un sujet PFE est obligatoire pour un stage PFE");
            }
            if (request.getSujetChoix2Id() != null &&
                    request.getSujetChoix1Id().equals(request.getSujetChoix2Id())) {
                throw new RuntimeException("Les deux choix de sujets PFE doivent être différents");
            }
        } else {
            if (request.getSujetChoix1Id() != null || request.getSujetChoix2Id() != null) {
                throw new RuntimeException("Les sujets PFE ne sont pas requis pour ce type de stage");
            }
        }

        if (Boolean.TRUE.equals(request.getEstBinome()) && request.getCandidat2() == null) {
            throw new RuntimeException("Les informations du binôme sont obligatoires");
        }
    }

    public CandidatureResponse modifier(UUID id, CandidatureUpdateRequest request) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));

        candidature.setStatut(request.getStatut());
        if (request.getDateEntretien() != null) {
            candidature.setDateEntretien(request.getDateEntretien());
        }

        Candidature updated = candidatureRepository.save(candidature);

        // Notifier n8n — envoi email changement de statut
        envoyerWebhookStatut(updated);

        return toResponse(updated);
    }

    /**
     * Endpoint dédié au changement de statut d'une candidature.
     * Déclenche automatiquement le webhook n8n avec toutes les informations
     * nécessaires pour :
     * - PRESELECTIONNE → n8n crée un Google Meet avec dateEntretien + envoie
     * l'email avec le lien
     * - ACCEPTE → n8n envoie l'email d'acceptation avec dateDebut, typeStage,
     * departement,
     * et si PFE : titre sujet, spécialité, durée
     * - REJETE → n8n envoie l'email de refus
     */
    @Transactional
    public CandidatureResponse changerStatut(UUID id, StatutCandidatureRequest request) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));

        // Si PRESELECTIONNE, la date d'entretien est obligatoire pour créer le Meet
        if (request.getStatut() == StatutCandidature.PRESELECTIONNE && request.getDateEntretien() == null) {
            throw new RuntimeException(
                    "La date d'entretien est obligatoire lorsque le statut est PRESELECTIONNE " +
                            "(n8n en a besoin pour créer le Google Meet).");
        }

        // Si ACCEPTE, la date de début de stage est obligatoire pour l'email de
        // confirmation
        if (request.getStatut() == StatutCandidature.ACCEPTE && request.getDateDebut() == null) {
            throw new RuntimeException(
                    "La date de début du stage est obligatoire lorsque le statut est ACCEPTE " +
                            "(elle sera incluse dans l'email envoyé au candidat).");
        }

        candidature.setStatut(request.getStatut());
        if (request.getDateEntretien() != null) {
            candidature.setDateEntretien(request.getDateEntretien());
        }
        if (request.getDateDebut() != null) {
            candidature.setDateDebut(request.getDateDebut());
        }
        if (request.getStatut() == StatutCandidature.ACCEPTE || request.getStatut() == StatutCandidature.REFUSE) {
            candidature.setDateTraitement(java.time.LocalDateTime.now());
        }

        Candidature updated = candidatureRepository.save(candidature);

        // Déclencher n8n avec le payload enrichi
        envoyerWebhookStatut(updated);

        return toResponse(updated);
    }

    public List<CandidatureResponse> listerTous() {
        return candidatureRepository.findAllOrderByDateDepotDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerParStatut(StatutCandidature statut) {
        return candidatureRepository.findByStatut(statut)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerParTypeStage(TypeStage typeStage) {
        return candidatureRepository.findByTypeStage(typeStage)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CandidatureResponse trouverParId(UUID id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));
        return toResponse(candidature);
    }

    public Long compterParStatut(StatutCandidature statut) {
        return candidatureRepository.countByStatut(statut);
    }

    public Long compterParTypeStage(TypeStage typeStage) {
        return candidatureRepository.countByTypeStage(typeStage);
    }

    private CandidatureResponse toResponse(Candidature candidature) {
        CandidatureResponse.CandidatureResponseBuilder builder = CandidatureResponse.builder()
                .id(candidature.getId())
                .typeStage(candidature.getTypeStage())
                .session(candidature.getSession())
                .annee(candidature.getAnnee())
                .anneeAcademique(candidature.getAnneeAcademique())
                .statut(candidature.getStatut())
                .estBinome(candidature.getEstBinome())
                .avertissementDoublonAccepte(candidature.getAvertissementDoublonAccepte())
                .dateDepot(candidature.getDateDepot())
                .dateEntretien(candidature.getDateEntretien())
                .dateTraitement(candidature.getDateTraitement())
                .dateDebut(candidature.getDateDebut())
                .statutDocuments(candidature.getStatutDocuments())
                .conventionValidee(candidature.getConventionValidee())
                .statutGlobal(candidature.getStatutGlobal())
                .dateAffectation(candidature.getDateAffectation())
                .candidat1(candidatService.toResponse(candidature.getCandidat1()))
                .candidat2(candidature.getCandidat2() != null
                        ? candidatService.toResponse(candidature.getCandidat2())
                        : null)
                .sujetChoix1(candidature.getSujetChoix1() != null
                        ? toSujetPfeSimpleResponse(candidature.getSujetChoix1())
                        : null)
                .sujetChoix2(candidature.getSujetChoix2() != null
                        ? toSujetPfeSimpleResponse(candidature.getSujetChoix2())
                        : null);

        // Ajouter les infos de département et encadrant si affectés
        if (candidature.getDepartement() != null) {
            builder.departementId(candidature.getDepartement().getId())
                    .departementNom(candidature.getDepartement().getNom());
        }
        if (candidature.getEncadrant() != null) {
            builder.encadrantId(candidature.getEncadrant().getId())
                    .encadrantNom(candidature.getEncadrant().getNom() + " " + candidature.getEncadrant().getPrenom());
        }

        return builder.build();
    }

    /**
     * Convertit DoublonCheckResult en DoublonCheckResponse pour le frontend
     */
    private DoublonCheckResponse toDoublonCheckResponse(DoublonCheckResult result) {
        return DoublonCheckResponse.builder()
                .niveau(result.getNiveau().name())
                .isBlocking(result.getIsBlocking())
                .technicalMessage(result.getMessage())
                .userMessage(result.getUserMessage())
                .details(result.getDetails())
                .build();
    }

    private SujetPfeSimpleResponse toSujetPfeSimpleResponse(SujetPfe sujet) {
        return SujetPfeSimpleResponse.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .departementNom(sujet.getDepartement().getNom())
                .dureeEnMois(sujet.getDureeEnMois())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PHASE 1 — Workflow RH : Candidature
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Préselectionner une candidature
     * Transition : EN_ATTENTE → PRESELECTIONNE
     * Préalable : la candidature doit être en statut EN_ATTENTE
     * n8n sera notifié pour préparer le mail de présélection
     */
    @Transactional
    public CandidatureResponse preselectionner(UUID candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée : " + candidatureId));

        if (candidature.getStatut() != StatutCandidature.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Seules les candidatures EN_ATTENTE peuvent être présélectionnées. " +
                            "Statut actuel : " + candidature.getStatut());
        }

        candidature.setStatut(StatutCandidature.PRESELECTIONNE);
        Candidature updated = candidatureRepository.save(candidature);

        log.info("Candidature {} présélectionnée", candidatureId);
        return toResponse(updated);
    }

    /**
     * Planifier l'entretien d'une candidature
     * Transition : PRESELECTIONNE → ENTRETIEN
     * Paramètre obligatoire : dateEntretien (LocalDateTime)
     * n8n sera notifié pour créer le Google Meet et envoyer l'email avec le lien
     */
    @Transactional
    public CandidatureResponse planiferEntretien(UUID candidatureId,
            CandidatureUpdateRequest request) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée : " + candidatureId));

        if (candidature.getStatut() != StatutCandidature.PRESELECTIONNE) {
            throw new IllegalStateException(
                    "Seules les candidatures PRéSELECTIONNEES peuvent passer en entretien. " +
                            "Statut actuel : " + candidature.getStatut());
        }

        if (request.getDateEntretien() == null) {
            throw new IllegalArgumentException("La date d'entretien est obligatoire");
        }

        candidature.setStatut(StatutCandidature.ENTRETIEN);
        candidature.setDateEntretien(request.getDateEntretien());
        Candidature updated = candidatureRepository.save(candidature);

        // Notifier n8n pour créer le Meet et envoyer l'email
        envoyerWebhookStatut(updated);

        log.info("Candidature {} : entretien planifié pour {}", candidatureId, request.getDateEntretien());
        return toResponse(updated);
    }

    /**
     * Accepter une candidature après entretien
     * Transition : ENTRETIEN → ACCEPTE
     * Paramètres obligatoires :
     * - dateDebut (LocalDate) : début du stage
     * - dateFin (LocalDate) optionnel : fin du stage (calculée si non fournie)
     * n8n sera notifié pour envoyer l'email d'acceptation avec les dates
     */
    @Transactional
    public CandidatureResponse accepterCandidature(UUID candidatureId,
            CandidatureUpdateRequest request) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée : " + candidatureId));

        if (candidature.getStatut() != StatutCandidature.ENTRETIEN) {
            throw new IllegalStateException(
                    "Seules les candidatures en ENTRETIEN peuvent être acceptées. " +
                            "Statut actuel : " + candidature.getStatut());
        }

        if (request.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début du stage est obligatoire pour l'acceptation");
        }

        candidature.setStatut(StatutCandidature.ACCEPTE);
        candidature.setDateDebut(request.getDateDebut());
        candidature.setDateTraitement(java.time.LocalDateTime.now());

        Candidature updated = candidatureRepository.save(candidature);

        // Notifier n8n pour envoyer l'email d'acceptation
        envoyerWebhookStatut(updated);

        log.info("Candidature {} acceptée, stage débute le {}", candidatureId, request.getDateDebut());
        return toResponse(updated);
    }

    /**
     * Refuser une candidature
     * Transition : * → REFUSE (depuis n'importe quel état)
     * Utilisée si le RH refuse après examen ou entretien
     * n8n sera notifié pour envoyer l'email de refus
     */
    @Transactional
    public CandidatureResponse refuserCandidature(UUID candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée : " + candidatureId));

        StatutCandidature ancienStatut = candidature.getStatut();

        candidature.setStatut(StatutCandidature.REFUSE);
        candidature.setDateTraitement(java.time.LocalDateTime.now());
        Candidature updated = candidatureRepository.save(candidature);

        // Notifier n8n pour envoyer l'email de refus
        envoyerWebhookStatut(updated);

        log.info("Candidature {} refusée (ancien statut : {})", candidatureId, ancienStatut);
        return toResponse(updated);
    }

    // ─────────────────────────────────────────────
    // Méthodes d'intégration n8n
    // ─────────────────────────────────────────────

    private void envoyerWebhookConfirmation(Candidature candidature) {
        if (!n8nEnabled || webhookConfirmationUrl == null || webhookConfirmationUrl.isBlank())
            return;

        Candidat c1 = candidature.getCandidat1();
        Candidat c2 = candidature.getCandidat2();
        SujetPfe sujet1 = candidature.getSujetChoix1();

        Map<String, Object> payload = new HashMap<>();

        // ── Infos candidature ──
        payload.put("candidatureId", candidature.getId().toString());
        payload.put("typeStage", candidature.getTypeStage().name());
        payload.put("dateDepot", candidature.getDateDepot().toString());
        payload.put("estBinome", candidature.getEstBinome());

        // ── Candidat principal ──
        payload.put("candidat1Nom", c1.getNom());
        payload.put("candidat1Prenom", c1.getPrenom());
        payload.put("candidat1Email", c1.getEmail());
        payload.put("universite", c1.getUniversite() != null ? c1.getUniversite().getNom() : "");

        // ── Candidat binôme (si présent) ──
        payload.put("candidat2Nom", c2 != null ? c2.getNom() : "");
        payload.put("candidat2Prenom", c2 != null ? c2.getPrenom() : "");
        payload.put("candidat2Email", c2 != null ? c2.getEmail() : "");

        // ── Sujet PFE (si typeStage = PFE et sujet choisi) ──
        if (sujet1 != null) {
            payload.put("pfeSujetTitre", sujet1.getTitre());
            payload.put("pfeDescription", sujet1.getMission());
            payload.put("pfeDepartement", sujet1.getDepartement() != null ? sujet1.getDepartement().getNom() : "");
            payload.put("pfeDuree", sujet1.getDureeEnMois() + " mois");

            // Compétences / technologies (les 3 premières)
            String[] competences = sujet1.getCompetences().stream()
                    .map(c -> c.getNom())
                    .limit(3)
                    .toArray(String[]::new);
            payload.put("pfeTech1", competences.length > 0 ? competences[0] : "");
            payload.put("pfeTech2", competences.length > 1 ? competences[1] : "");
            payload.put("pfeTech3", competences.length > 2 ? competences[2] : "");

            // Spécialité (la première trouvée)
            String specialite = sujet1.getSpecialitesUniversitaires().stream()
                    .map(s -> s.getNom())
                    .findFirst()
                    .orElse("");
            payload.put("pfeSpecialite", specialite);
        } else {
            payload.put("pfeSujetTitre", "");
            payload.put("pfeDescription", "");
            payload.put("pfeDepartement", "");
            payload.put("pfeDuree", "");
            payload.put("pfeTech1", "");
            payload.put("pfeTech2", "");
            payload.put("pfeTech3", "");
            payload.put("pfeSpecialite", "");
        }

        notifierN8n(webhookConfirmationUrl, payload, "confirmation candidature");
    }

    private void envoyerWebhookStatut(Candidature candidature) {
        if (!n8nEnabled || webhookStatutUrl == null || webhookStatutUrl.isBlank())
            return;

        Candidat c1 = candidature.getCandidat1();
        Candidat c2 = candidature.getCandidat2();
        SujetPfe sujet1 = candidature.getSujetChoix1();
        StatutCandidature statut = candidature.getStatut();

        // Clé de routage : n8n utilise ce champ dans un Switch node pour choisir
        // l'action email
        String n8nAction = switch (statut) {
            case PRESELECTIONNE -> "ENVOYER_EMAIL_PRESELECTION_AVEC_MEET";
            case ACCEPTE -> "ENVOYER_EMAIL_ACCEPTATION";
            case REFUSE -> "ENVOYER_EMAIL_REFUS";
            default -> "AUCUNE_ACTION";
        };

        Map<String, Object> payload = new HashMap<>();

        // ── Métadonnées candidature ──
        payload.put("candidatureId", candidature.getId().toString());
        payload.put("typeStage", candidature.getTypeStage().name());
        payload.put("nouveauStatut", statut.name());
        payload.put("n8nAction", n8nAction);
        payload.put("estBinome", candidature.getEstBinome());

        // ── dateEntretien (pour PRESELECTIONNE / Google Meet) ──
        if (candidature.getDateEntretien() != null) {
            payload.put("dateEntretien",
                    candidature.getDateEntretien().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            payload.put("dateEntretienDate",
                    candidature.getDateEntretien().format(DateTimeFormatter.ISO_LOCAL_DATE));
            payload.put("dateEntretienHeure",
                    candidature.getDateEntretien().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            payload.put("dateEntretien", null);
            payload.put("dateEntretienDate", null);
            payload.put("dateEntretienHeure", null);
        }

        // ── dateDebut (pour ACCEPTE — inclus dans l'email de confirmation) ──
        if (candidature.getDateDebut() != null) {
            payload.put("dateDebut",
                    candidature.getDateDebut().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            // Version lisible pour l'email : "01/05/2026"
            payload.put("dateDebutFormatee",
                    candidature.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            payload.put("dateDebut", null);
            payload.put("dateDebutFormatee", null);
        }

        // ── Candidat principal ──
        payload.put("candidat1Nom", c1.getNom());
        payload.put("candidat1Prenom", c1.getPrenom());
        payload.put("candidat1Email", c1.getEmail());
        payload.put("candidat1Tel", c1.getTel());
        payload.put("universite", c1.getUniversite() != null ? c1.getUniversite().getNom() : "");

        // ── Candidat binôme (si présent) ──
        payload.put("candidat2Nom", c2 != null ? c2.getNom() : null);
        payload.put("candidat2Prenom", c2 != null ? c2.getPrenom() : null);
        payload.put("candidat2Email", c2 != null ? c2.getEmail() : null);
        payload.put("candidat2Tel", c2 != null ? c2.getTel() : null);

        // ── Informations PFE / Département ──
        // Toujours envoyées : le département (présent même pour les stages non-PFE via
        // le sujet)
        if (sujet1 != null) {
            // Département (utile pour tous les types de stage avec sujet)
            payload.put("departement", sujet1.getDepartement() != null
                    ? sujet1.getDepartement().getNom()
                    : null);

            // Champs spécifiques PFE (titre, spécialité, durée)
            payload.put("pfeSujetTitre", sujet1.getTitre());
            payload.put("pfeDuree", sujet1.getDureeEnMois() + " mois");

            // Spécialité(s) — toutes les spécialités concaténées, ou la première
            String specialites = sujet1.getSpecialitesUniversitaires().stream()
                    .map(SpecialiteUniversitaire::getNom)
                    .collect(java.util.stream.Collectors.joining(", "));
            payload.put("pfeSpecialite", specialites.isEmpty() ? null : specialites);
        } else {
            payload.put("departement", null);
            payload.put("pfeSujetTitre", null);
            payload.put("pfeDuree", null);
            payload.put("pfeSpecialite", null);
        }

        notifierN8n(webhookStatutUrl, payload, "changement statut [" + statut.name() + "]");
    }

    private void notifierN8n(String url, Map<String, Object> payload, String contexte) {
        try {
            restTemplate.postForEntity(url, payload, String.class);
            log.info("[n8n] Webhook '{}' envoyé avec succès → {}", contexte, url);
        } catch (Exception e) {
            // Non bloquant : l'opération métier ne doit pas échouer si n8n est indisponible
            log.error("[n8n] Échec envoi webhook '{}' → {} : {}", contexte, url, e.getMessage());
        }
    }
}
