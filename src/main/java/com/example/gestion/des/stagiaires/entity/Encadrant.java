package com.example.gestion.des.stagiaires.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "encadrants")
public class Encadrant extends Utilisateur {

    @Column(name = "capacite_max", nullable = false)
    private Integer capaciteMax;

    @Builder.Default
    @Column(name = "capacite_actuelle")
    private Integer capaciteActuelle = 0;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "departement_id", referencedColumnName = "id", nullable = false)
    private Departement departement;

    @ManyToMany
    @JoinTable(name = "encadrant_specialites", joinColumns = @JoinColumn(name = "encadrant_id"), inverseJoinColumns = @JoinColumn(name = "specialite_id"))
    @Builder.Default
    private Set<Specialite> specialites = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "encadrant_competences", joinColumns = @JoinColumn(name = "encadrant_id"), inverseJoinColumns = @JoinColumn(name = "competence_id"))
    @Builder.Default
    private Set<Competence> competences = new HashSet<>();
}
