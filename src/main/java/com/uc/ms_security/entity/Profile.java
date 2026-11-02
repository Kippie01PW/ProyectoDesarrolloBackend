package com.uc.ms_security.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 30
    )
    private String phone;

    @Column(
            name = "birth_date",
            nullable = false
    )
    private LocalDate birthDate;

    @Column(
            name = "photo_url",
            columnDefinition = "MEDIUMTEXT"
    )
    private String photoUrl;

    @OneToOne(
            fetch = FetchType.LAZY //Carga por partes, osea solo cargar el perfil cuando se llame al usuario
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;
}