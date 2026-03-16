package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.SpecialiteUniversitaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialiteUniversitaireRepository extends JpaRepository<SpecialiteUniversitaire, Long> {
    boolean existsByNom(String nom);

    List<SpecialiteUniversitaire> findAllByIdIn(List<Long> ids);
}
