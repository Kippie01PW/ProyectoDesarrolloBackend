package com.uc.ms_security.repository;

import com.uc.ms_security.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findAllByUserId(Long userId);

    Optional<Session> findByIdAndUserId(Long sessionId, Long userId);

    boolean existsByToken(String token);

    boolean existsByTokenAndIdNot(String token, Long id);

    Optional<Session> findByTokenAndActiveTrueAndExpirationAfter(
            String token, LocalDateTime now);

    Optional<Session> findByToken(String token);

    void deleteAllByUserId(Long userId);
}
