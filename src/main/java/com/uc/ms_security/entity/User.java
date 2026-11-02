package com.uc.ms_security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "auth_provider", nullable = false, length = 30)
    private String authProvider = "LOCAL";

    @Column(name = "provider_subject", unique = true, length = 200)
    private String providerSubject;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean enabled = true;

        @OneToOne(
                        mappedBy = "user",
                        cascade = CascadeType.ALL,
                        orphanRemoval = true,
                        fetch = FetchType.LAZY
        )
        private Profile profile;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Session> sessions = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<UserRole> userRoles = new ArrayList<>();
}
