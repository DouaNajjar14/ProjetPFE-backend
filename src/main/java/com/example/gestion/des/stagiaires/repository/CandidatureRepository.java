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

    // ═══════════════════════════════════════════════════════════════════════
    // REQUÊTES SPÉCIALISÉES — Détection des doublons
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Recherche candidatures existantes pour le même email, type et année
     * Exclut les candidatures refusées
     */
    @Query("SELECT c FROM Candidature c " +
            "WHERE c.candidat1.email = :email " +
            "AND c.typeStage = :typeStage " +
            "AND c.annee = :year " +
            "AND CAST(c.statut AS string) != 'REFUSE'")
    List<Candidature> findByEmailTypeYearActive(
            @Param("email") String email,
            @Param("typeStage") TypeStage typeStage,
            @Param("year") Integer year);

    /**
     * Recherche candidatures d'un candidat pour un type et année donnés
     */
    @Query("SELECT c FROM Candidature c " +
            "WHERE (c.candidat1.id = :candidatId OR c.candidat2.id = :candidatId) " +
            "AND c.typeStage = :typeStage " +
            "AND c.annee = :year " +
            "AND CAST(c.statut AS string) != 'REFUSE'")
    List<Candidature> findByCandidatAndTypeAndYear(
            @Param("candidatId") UUID candidatId,
            @Param("typeStage") TypeStage typeStage,
            @Param("year") Integer year);

    /**
     * Recherche candidatures avec le même nom et prénom mais email différent
     * Utilisé pour détecter les homonymes
     */
    @Query("SELECT c FROM Candidature c " +
            "WHERE c.candidat1.nom = :nom " +
            "AND c.candidat1.prenom = :prenom " +
            "AND c.candidat1.email != :email")
    List<Candidature> findByNomPrenomButDifferentEmail(
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("email") String email);

    /**
     * Compte le nombre de candidatures par statut pour une année donnée
     */
    @Query("SELECT COUNT(c) FROM Candidature c " +
            "WHERE c.statut = :statut AND c.annee = :year")
    Long countByStatutAndYear(
            @Param("statut") StatutCandidature statut,
            @Param("year") Integer year);

    /**
     * Compte le nombre de candidatures par type et année
     */
    @Query("SELECT COUNT(c) FROM Candidature c " +
            "WHERE c.typeStage = :typeStage AND c.annee = :year")
    Long countByTypeStageAndYear(
            @Param("typeStage") TypeStage typeStage,
            @Param("year") Integer year);

    /**
     * Récupère les candidatures en attente pour un type de stage
     */
    @Query("SELECT c FROM Candidature c " +
            "WHERE c.typeStage = :typeStage " +
            "AND c.statut = 'EN_ATTENTE' " +
            "ORDER BY c.dateDepot ASC")
    List<Candidature> findPendingByTypeStage(@Param("typeStage") TypeStage typeStage);
}
