package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StagiaireRepository extends JpaRepository<Stagiaire, Long> {

    boolean existsByCandidatureId(UUID candidatureId);

    Optional<Stagiaire> findByCandidatureId(UUID candidatureId);

    List<Stagiaire> findAllByOrderByCreatedAtDesc();
}
