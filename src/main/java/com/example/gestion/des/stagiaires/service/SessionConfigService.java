package com.example.gestion.des.stagiaires.service;

import com.example.gestion.des.stagiaires.entity.SessionConfig;
import com.example.gestion.des.stagiaires.enums.SessionType;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.repository.SessionConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion des configurations de session
 * Table statique centralisée permettant à l'admin de modifier les fenêtres sans
 * code
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionConfigService {

    private final SessionConfigRepository sessionConfigRepository;

    /**
     * Récupère la configuration pour un type de stage
     */
    @Transactional(readOnly = true)
    public SessionConfig getConfigByTypeStage(TypeStage typeStage) {
        return sessionConfigRepository.findByTypeStage(typeStage)
                .orElseThrow(() -> new RuntimeException("Configuration de session non trouvée pour : " + typeStage));
    }

    /**
     * Récupère le type de session dérivé du type de stage
     * Règle : INITIATION/PERFECTIONNEMENT → HIVER | PFE → PFE | ETE → ETE
     */
    public SessionType deriveSessionType(TypeStage typeStage) {
        SessionConfig config = getConfigByTypeStage(typeStage);
        return config.getSessionType();
    }

    /**
     * Récupère la configuration pour un type de session
     */
    @Transactional(readOnly = true)
    public SessionConfig getConfigBySessionType(SessionType sessionType) {
        return sessionConfigRepository.findBySessionType(sessionType)
                .orElseThrow(() -> new RuntimeException("Configuration de session non trouvée pour : " + sessionType));
    }
}
