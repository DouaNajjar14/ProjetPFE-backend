package com.example.gestion.des.stagiaires.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "departements")
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String responsable;

    @Builder.Default
    private Integer nombreEncadrantsActuel = 0;

    @Builder.Default
    private Integer nombreStagiairesActuel = 0;

    @Builder.Default
    private Boolean archive = false;
}
