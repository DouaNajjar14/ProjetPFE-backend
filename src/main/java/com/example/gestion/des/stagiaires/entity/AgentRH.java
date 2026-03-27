package com.example.gestion.des.stagiaires.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@Table(name = "agents_rh")
@PrimaryKeyJoinColumn(name = "id")
public class AgentRH extends Utilisateur {

}
