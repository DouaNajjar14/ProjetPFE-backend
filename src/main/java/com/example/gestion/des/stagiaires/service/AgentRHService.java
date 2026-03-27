package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.*;
import com.example.gestion.des.stagiaires.entity.SujetPfe;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.entity.AgentRH;
import com.example.gestion.des.stagiaires.enums.Role;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.CandidatureRepository;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import com.example.gestion.des.stagiaires.repository.AgentRHRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentRHService {

    private final AgentRHRepository agentRHRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final CandidatureRepository candidatureRepository;
    private final CandidatService candidatService;

    public AgentRHResponse creer(AgentRHRequest request) {
        // Vérifier email dans la base complète
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Créer un AgentRH (pas un Utilisateur générique)
        AgentRH agent = AgentRH.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .tel(request.getTel())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(Role.AGENT_RH)
                .actif(true)
                .build();

        // Sauvegarder dans la table agents_rh via JOINED inheritance
        AgentRH saved = agentRHRepository.save(agent);
        return toResponse(saved);
    }

    public AgentRHResponse modifier(UUID id, AgentRHUpdateRequest request) {
        AgentRH agent = agentRHRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!agent.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        agent.setNom(request.getNom());
        agent.setPrenom(request.getPrenom());
        agent.setEmail(request.getEmail());
        agent.setTel(request.getTel());

        AgentRH updated = agentRHRepository.save(agent);
        return toResponse(updated);
    }

    public AgentRHResponse archiver(UUID id) {
        AgentRH agent = agentRHRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        agent.setActif(false);
        AgentRH archived = agentRHRepository.save(agent);
        return toResponse(archived);
    }

    public AgentRHResponse desarchiver(UUID id) {
        AgentRH agent = agentRHRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        agent.setActif(true);
        AgentRH unarchived = agentRHRepository.save(agent);
        return toResponse(unarchived);
    }

    public List<AgentRHResponse> listerActifs() {
        return agentRHRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgentRHResponse> listerArchives() {
        return agentRHRepository.findArchives()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgentRHResponse> listerTous() {
        return agentRHRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AgentRHResponse trouverParId(UUID id) {
        AgentRH agent = agentRHRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));
        return toResponse(agent);
    }

    private AgentRHResponse toResponse(AgentRH agent) {
        return AgentRHResponse.builder()
                .id(agent.getId())
                .nom(agent.getNom())
                .prenom(agent.getPrenom())
                .email(agent.getEmail())
                .tel(agent.getTel())
                .actif(agent.getActif())
                .dateCreation(agent.getDateCreation())
                .dateModification(agent.getDateModification())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Consultation des candidatures (lecture seule)
    // ─────────────────────────────────────────────────────────────────────────

    public List<CandidatureResponse> listerToutesCandidatures() {
        return candidatureRepository.findAllOrderByDateDepotDesc()
                .stream()
                .map(this::toCandidatureResponse)
                .collect(Collectors.toList());
    }

    public CandidatureResponse trouverCandidatureParId(UUID id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'id : " + id));
        return toCandidatureResponse(candidature);
    }

    public List<CandidatureResponse> listerCandidaturesParStatut(StatutCandidature statut) {
        return candidatureRepository.findByStatut(statut)
                .stream()
                .map(this::toCandidatureResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerCandidaturesParTypeStage(TypeStage typeStage) {
        return candidatureRepository.findByTypeStage(typeStage)
                .stream()
                .map(this::toCandidatureResponse)
                .collect(Collectors.toList());
    }

    public List<CandidatureResponse> listerCandidaturesParTypeEtStatut(TypeStage typeStage, StatutCandidature statut) {
        return candidatureRepository.findByTypeStageAndStatut(typeStage, statut)
                .stream()
                .map(this::toCandidatureResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistiquesCandidatures() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", candidatureRepository.count());

        Map<String, Long> parStatut = new HashMap<>();
        for (StatutCandidature s : StatutCandidature.values()) {
            parStatut.put(s.name(), candidatureRepository.countByStatut(s));
        }
        stats.put("parStatut", parStatut);

        Map<String, Long> parType = new HashMap<>();
        for (TypeStage t : TypeStage.values()) {
            parType.put(t.name(), candidatureRepository.countByTypeStage(t));
        }
        stats.put("parTypeStage", parType);

        return stats;
    }

    private CandidatureResponse toCandidatureResponse(Candidature c) {
        return CandidatureResponse.builder()
                .id(c.getId())
                .typeStage(c.getTypeStage())
                .statut(c.getStatut())
                .estBinome(c.getEstBinome())
                .dateDepot(c.getDateDepot())
                .dateEntretien(c.getDateEntretien())
                .candidat1(candidatService.toResponse(c.getCandidat1()))
                .candidat2(c.getCandidat2() != null ? candidatService.toResponse(c.getCandidat2()) : null)
                .sujetChoix1(c.getSujetChoix1() != null ? toSujetSimple(c.getSujetChoix1()) : null)
                .sujetChoix2(c.getSujetChoix2() != null ? toSujetSimple(c.getSujetChoix2()) : null)
                .build();
    }

    private SujetPfeSimpleResponse toSujetSimple(SujetPfe sujet) {
        return SujetPfeSimpleResponse.builder()
                .id(sujet.getId())
                .titre(sujet.getTitre())
                .departementNom(sujet.getDepartement().getNom())
                .dureeEnMois(sujet.getDureeEnMois())
                .build();
    }
}
