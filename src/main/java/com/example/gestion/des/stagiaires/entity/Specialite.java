package com.example.gestion.des.stagiaires.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "specialites")
public class Specialite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    @OneToMany(mappedBy = "specialite", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Competence> competences = new ArrayList<>();
}
