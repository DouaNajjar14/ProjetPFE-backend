package com.example.gestion.des.stagiaires.entity;

import com.example.gestion.des.stagiaires.enums.SessionType;
import com.example.gestion.des.stagiaires.enums.TypeStage;
import com.example.gestion.des.stagiaires.converter.MonthDayConverter;
import jakarta.persistence.*;
import java.time.MonthDay;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.*;

/**
 * SessionConfig — Configuration statique des fenêtres de stage
 * Table de 4 lignes définissant les périodes exactes de chaque session
 * Modification possible via l'interface admin sans toucher au code
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "session_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = "type_stage")
})
public class SessionConfig {

    /**
     * PK : TypeStage (unique) — INITIATION, PERFECTIONNEMENT, PFE, ETE
     * Chaque type de stage a exactement une configuration
     */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "type_stage", nullable = false, unique = true)
    private TypeStage typeStage;

    /**
     * SessionType dérivé
     * Relation : INITIATION/PERFECTIONNEMENT → HIVER | PFE → PFE | ETE → ETE
     */
    @Column(name = "session_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    /**
     * Label lisible : "Session Hiver", "Session PFE", "Session Été"
     */
    @Column(name = "label", nullable = false, length = 255)
    private String label;

    /**
     * dateDebutFixe — Début de la fenêtre (MonthDay : mois+jour, pas d'année)
     * Valeurs :
     * - INITIATION : 01-07 (7 janvier)
     * - PERFECTIONNEMENT : 01-07 (7 janvier)
     * - PFE : 02-01 (1er février)
     * - ETE : 07-01 (1er juillet)
     */
    @Convert(converter = MonthDayConverter.class)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "date_debut_fixe", nullable = false, columnDefinition = "VARCHAR(20)")
    private MonthDay dateDebutFixe;

    /**
     * dateFinFixe — Fin de la fenêtre (MonthDay : mois+jour, pas d'année)
     * Valeurs :
     * - INITIATION : 02-07 (7 février)
     * - PERFECTIONNEMENT : 02-07 (7 février)
     * - PFE : null (la fin est variable, lue sur la convention)
     * - ETE : 08-31 (31 août)
     *
     * null signifie que la fin est variable et sera renseignée individuellement
     * par le RH après validation de la convention de stage
     */
    @Convert(converter = MonthDayConverter.class)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "date_fin_fixe", nullable = true, columnDefinition = "VARCHAR(20)")
    private MonthDay dateFinFixe;

    /**
     * dureeEnMoisFixe — Durée minimale attendue (optionnel, informatif)
     * Valeurs :
     * - INITIATION : 1
     * - PERFECTIONNEMENT : 1
     * - PFE : 4 à 6
     * - ETE : 1 à 2
     */
    @Column(name = "duree_en_mois_fixe", nullable = true)
    private Integer dureeEnMoisFixe;

    /**
     * Description ou notes pour l'admin
     */
    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;
}
