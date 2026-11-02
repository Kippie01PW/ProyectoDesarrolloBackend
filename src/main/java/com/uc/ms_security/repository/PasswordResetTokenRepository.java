package com.uc.ms_security.repository;

import com.uc.ms_security.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.EntityGraph;   
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;                                          
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpirationAfter(
            String token, LocalDateTime now);

    //busca solo por el token; el service decide si es válido
    @EntityGraph(attributePaths = {"user"})
    Optional<PasswordResetToken> findByToken(String token);

    //tokens aún sin usar de un usuario, para invalidarlos
    List<PasswordResetToken> findAllByUserIdAndUsedFalse(Long userId);
}
