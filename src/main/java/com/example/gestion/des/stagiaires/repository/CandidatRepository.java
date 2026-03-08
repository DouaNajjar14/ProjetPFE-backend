package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, UUID> {

    Optional<Candidat> findByEmail(String email);

    boolean existsByEmail(String email);
}
