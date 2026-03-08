package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.dto.AgentRHRequest;
import com.example.gestion.des.stagiaires.dto.AgentRHResponse;
import com.example.gestion.des.stagiaires.dto.AgentRHUpdateRequest;
import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.enums.Role;
import com.example.gestion.des.stagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentRHService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AgentRHResponse creer(AgentRHRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        Utilisateur agent = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .tel(request.getTel())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(Role.AGENT_RH)
                .actif(true)
                .build();

        Utilisateur saved = utilisateurRepository.save(agent);
        return toResponse(saved);
    }

    public AgentRHResponse modifier(UUID id, AgentRHUpdateRequest request) {
        Utilisateur agent = utilisateurRepository.findByIdAndRole(id, Role.AGENT_RH)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!agent.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        agent.setNom(request.getNom());
        agent.setPrenom(request.getPrenom());
        agent.setEmail(request.getEmail());
        agent.setTel(request.getTel());

        Utilisateur updated = utilisateurRepository.save(agent);
        return toResponse(updated);
    }

    public AgentRHResponse archiver(UUID id) {
        Utilisateur agent = utilisateurRepository.findByIdAndRole(id, Role.AGENT_RH)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        agent.setActif(false);
        Utilisateur archived = utilisateurRepository.save(agent);
        return toResponse(archived);
    }

    public AgentRHResponse desarchiver(UUID id) {
        Utilisateur agent = utilisateurRepository.findByIdAndRole(id, Role.AGENT_RH)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));

        agent.setActif(true);
        Utilisateur unarchived = utilisateurRepository.save(agent);
        return toResponse(unarchived);
    }

    public List<AgentRHResponse> listerActifs() {
        return utilisateurRepository.findByRoleAndActif(Role.AGENT_RH, true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgentRHResponse> listerArchives() {
        return utilisateurRepository.findByRoleAndActif(Role.AGENT_RH, false)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AgentRHResponse> listerTous() {
        return utilisateurRepository.findByRole(Role.AGENT_RH)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AgentRHResponse trouverParId(UUID id) {
        Utilisateur agent = utilisateurRepository.findByIdAndRole(id, Role.AGENT_RH)
                .orElseThrow(() -> new RuntimeException("Agent RH non trouvé avec l'id : " + id));
        return toResponse(agent);
    }

    private AgentRHResponse toResponse(Utilisateur agent) {
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
}

