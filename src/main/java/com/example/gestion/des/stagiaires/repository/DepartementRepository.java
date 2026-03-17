package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartementRepository extends JpaRepository<Departement, UUID> {

    List<Departement> findByArchiveFalse();

    List<Departement> findByArchiveTrue();

    boolean existsByNomAndArchiveFalse(String nom);

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, UUID id);
}
