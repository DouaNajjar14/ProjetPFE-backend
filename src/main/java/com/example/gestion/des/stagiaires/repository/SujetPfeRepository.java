package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.SujetPfe;
import com.example.gestion.des.stagiaires.enums.STATUT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SujetPfeRepository extends JpaRepository<SujetPfe, UUID> {

    Page<SujetPfe> findByArchiveFalse(Pageable pageable);

    Page<SujetPfe> findByArchiveTrue(Pageable pageable);

    Page<SujetPfe> findByStatutAndArchiveFalse(STATUT statut, Pageable pageable);

    // Méthodes sans pagination pour l'API publique
    List<SujetPfe> findByStatutAndArchiveFalse(STATUT statut);

    List<SujetPfe> findByDepartementIdAndStatutAndArchiveFalse(UUID departementId, STATUT statut);

    Page<SujetPfe> findByDepartement_IdAndArchiveFalse(UUID departementId, Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM SujetPfe s
            LEFT JOIN s.specialitesUniversitaires spec
            WHERE s.archive = false
            AND (:statut IS NULL OR s.statut = :statut)
            AND (:departementId IS NULL OR s.departement.id = :departementId)
            AND (:specialiteId IS NULL OR spec.id = :specialiteId)
            AND (
                :titre IS NULL OR :titre = ''
                OR LOWER(s.titre) LIKE LOWER(CONCAT('%', :titre, '%'))
            )
            """)
    Page<SujetPfe> rechercher(
            @Param("statut") STATUT statut,
            @Param("departementId") UUID departementId,
            @Param("titre") String titre,
            @Param("specialiteId") Long specialiteId,
            Pageable pageable);
}
