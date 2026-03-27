package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.SessionConfig;
import com.example.gestion.des.stagiaires.enums.SessionType;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionConfigRepository extends JpaRepository<SessionConfig, TypeStage> {

    Optional<SessionConfig> findByTypeStage(TypeStage typeStage);

    Optional<SessionConfig> findBySessionType(SessionType sessionType);
}
