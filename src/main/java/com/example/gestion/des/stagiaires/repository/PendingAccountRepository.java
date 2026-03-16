package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.PendingAccount;
import com.example.gestion.des.stagiaires.enums.PendingAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingAccountRepository extends JpaRepository<PendingAccount, UUID> {

    Optional<PendingAccount> findByToken(String token);

    boolean existsByUsername(String username);

    boolean existsByCandidatureIdAndStatut(UUID candidatureId, PendingAccountStatus statut);

    List<PendingAccount> findByStatut(PendingAccountStatus statut);

    @Modifying
    @Query("UPDATE PendingAccount p SET p.statut = 'EXPIRE' " +
           "WHERE p.statut = 'EN_ATTENTE' AND p.expiresAt < :now")
    int expireOldTokens(LocalDateTime now);
}

