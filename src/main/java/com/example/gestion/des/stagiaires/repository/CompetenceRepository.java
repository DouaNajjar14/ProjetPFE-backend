package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Competence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {
    Optional<Competence> findByNom(String nom);

    boolean existsByNomAndSpecialiteId(String nom, Long specialiteId);

    boolean existsByNom(String nom);

    List<Competence> findByIdIn(List<Long> ids);

    List<Competence> findBySpecialiteId(Long specialiteId);
}
