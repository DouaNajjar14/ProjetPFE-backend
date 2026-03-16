package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {
    List<Specialite> findByDepartementId(UUID departementId);
    boolean existsByNomAndDepartementId(String nom, UUID departementId);
}
