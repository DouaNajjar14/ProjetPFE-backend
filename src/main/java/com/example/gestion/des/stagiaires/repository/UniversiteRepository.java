package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Universite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversiteRepository extends JpaRepository<Universite, UUID> {

    Optional<Universite> findByNom(String nom);

    boolean existsByNom(String nom);
}
