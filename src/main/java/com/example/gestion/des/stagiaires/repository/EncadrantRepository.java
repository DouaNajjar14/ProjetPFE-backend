package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Encadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncadrantRepository extends JpaRepository<Encadrant, UUID> {

    List<Encadrant> findByActif(Boolean actif);

    Optional<Encadrant> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Encadrant e WHERE e.capaciteActuelle < e.capaciteMax AND e.actif = true")
    List<Encadrant> findDisponibles();

    Long countByDepartementIdAndActifTrue(UUID departementId);

    List<Encadrant> findByDepartementId(UUID departementId);
}
