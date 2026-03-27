package com.example.gestion.des.stagiaires.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Table(name = "administrateurs")
@PrimaryKeyJoinColumn(name = "id")
public class Admin extends Utilisateur {

}
