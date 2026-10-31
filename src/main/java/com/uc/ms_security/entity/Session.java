package com.uc.ms_security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiration;

    @Column(name = "code_2fa", length = 10)
    private String code2FA;

    @Column(name = "code_2fa_hash", length = 100)
    private String code2FAHash;

    @Column(name = "code_2fa_expiration")
    private LocalDateTime code2FAExpiration;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
