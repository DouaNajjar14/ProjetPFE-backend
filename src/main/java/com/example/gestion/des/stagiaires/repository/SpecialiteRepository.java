package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialiteRepository extends JpaRepository<Specialite, Long> {
    List<Specialite> findByArchiveFalse();

    List<Specialite> findByDepartementIdAndArchiveFalse(UUID departementId);

    List<Specialite> findByDepartementIdAndArchiveTrue(UUID departementId);

    boolean existsByNomAndDepartementIdAndArchiveFalse(String nom, UUID departementId);

    boolean existsByNomAndDepartementIdAndArchiveFalseAndIdNot(String nom, UUID departementId, Long id);
}
