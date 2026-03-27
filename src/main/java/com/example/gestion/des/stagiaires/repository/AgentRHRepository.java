package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.AgentRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRHRepository extends JpaRepository<AgentRH, UUID> {

    List<AgentRH> findByActifTrueOrderByNomAsc();

    @Query("SELECT a FROM AgentRH a WHERE a.actif = false ORDER BY a.nom ASC")
    List<AgentRH> findArchives();

    Optional<AgentRH> findByEmail(String email);

    boolean existsByEmail(String email);
}
