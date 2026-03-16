package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Candidature;
import com.example.gestion.des.stagiaires.enums.StatutCandidature;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, UUID> {

    List<Candidature> findByStatut(StatutCandidature statut);

    List<Candidature> findByTypeStage(TypeStage typeStage);

    List<Candidature> findByTypeStageAndStatut(TypeStage typeStage, StatutCandidature statut);

    @Query("SELECT c FROM Candidature c WHERE c.candidat1.id = :candidatId OR c.candidat2.id = :candidatId")
    List<Candidature> findByCandidatId(@Param("candidatId") UUID candidatId);

    @Query("SELECT c FROM Candidature c WHERE c.sujetChoix1.id = :sujetId OR c.sujetChoix2.id = :sujetId")
    List<Candidature> findBySujetPfeId(@Param("sujetId") UUID sujetId);

    Long countByStatut(StatutCandidature statut);

    Long countByTypeStage(TypeStage typeStage);

    @Query("SELECT c FROM Candidature c ORDER BY c.dateDepot DESC")
    List<Candidature> findAllOrderByDateDepotDesc();
}
