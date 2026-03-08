package com.example.gestion.des.stagiaires.repository;

import com.example.gestion.des.stagiaires.entity.Utilisateur;
import com.example.gestion.des.stagiaires.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmailAndRole(String email, Role role);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByRoleAndActif(Role role, Boolean actif);

    Optional<Utilisateur> findByIdAndRole(UUID id, Role role);
}
