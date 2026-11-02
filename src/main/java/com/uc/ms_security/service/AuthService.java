package com.uc.ms_security.service;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.FirebaseLoginRequestDTO;
import com.uc.ms_security.dto.auth.LoginRequestDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryRequestDTO;
import com.uc.ms_security.dto.auth.PasswordResetRequestDTO;
import com.uc.ms_security.dto.auth.RegisterRequestDTO;
import com.uc.ms_security.dto.auth.TwoFactorVerifyRequestDTO;
import com.uc.ms_security.entity.PasswordResetToken;
import com.uc.ms_security.entity.Session;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.PasswordResetTokenRepository;
import com.uc.ms_security.repository.SessionRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.FirebaseApp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionRepository sessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final NotificationService notificationService;
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${security.2fa.code-duration-minutes:10}")
    private long twoFactorDurationMinutes;

    @Value("${security.password-reset-duration-minutes:30}")
    private long passwordResetDurationMinutes;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ApplicationException(ErrorCase.ALREADY_EXISTS,
                    "Ya existe un usuario con este email");
        }
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setAuthProvider("LOCAL");
        return userResponse(userRepository.save(user));
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

        if (!"LOCAL".equals(user.getAuthProvider())
                || user.getPassword() == null
                || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw unauthorized();
        }

        if (user.isTwoFactorEnabled()) {
            return createTwoFactorChallenge(user);
        }
        return createActiveSession(user);
    }

    public AuthResponseDTO verifyTwoFactor(TwoFactorVerifyRequestDTO dto) {
        Session session = sessionRepository.findByToken(dto.challengeToken())
                .orElseThrow(this::unauthorized);
        LocalDateTime now = LocalDateTime.now();
        if (session.isActive() || session.getCode2FAExpiration() == null
                || session.getCode2FAExpiration().isBefore(now)
                || !passwordEncoder.matches(dto.code(), session.getCode2FAHash())) {
            throw unauthorized();
        }
        session.setActive(true);
        session.setCode2FAHash(null);
        session.setCode2FA(null);
        session.setCode2FAExpiration(null);
        AuthResponseDTO response = createTokenResponse(session.getUser());
        session.setToken(response.accessToken());
        session.setExpiration(response.expiresAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        sessionRepository.save(session);
        return response;
    }

    public AuthResponseDTO firebaseLogin(FirebaseLoginRequestDTO dto) {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION,
                    "La autenticación Firebase no está configurada");
        }
        try {
            FirebaseToken token = FirebaseAuth.getInstance(firebaseApp)
                    .verifyIdToken(dto.idToken());
            String email = token.getEmail();
            if (email == null || email.isBlank()) {
                throw unauthorized();
            }
            if (!isEmailVerified(token)) {
                throw new ApplicationException(
                        ErrorCase.UNAUTHORIZED,
                        "El correo del proveedor no está verificado");
            }
            User user = userRepository.findByProviderSubject(token.getUid()).orElse(null);
            if (user == null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setName(token.getName() == null ? email : token.getName());
                    user.setAuthProvider("FIREBASE");
                } else if (user.getProviderSubject() != null
                        && !user.getProviderSubject().equals(token.getUid())) {
                    throw new ApplicationException(
                            ErrorCase.INVALID_OPERATION,
                            "El correo ya está vinculado a otra cuenta de Firebase");
                }
                user.setProviderSubject(token.getUid());
                if (!"LOCAL".equals(user.getAuthProvider())) {
                    user.setAuthProvider("FIREBASE");
                    user.setPassword(null);
                }
                user = userRepository.save(user);
            }
            return createActiveSession(user);
        } catch (FirebaseAuthException | IllegalArgumentException exception) {
            throw unauthorized();
        }
    }

    private boolean isEmailVerified(FirebaseToken token) {
        Object verified = token.getClaims().get("email_verified");
        return Boolean.TRUE.equals(verified);
    }

    public void requestPasswordRecovery(PasswordRecoveryRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(this::unauthorized);
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION,
                    "Los usuarios OAuth deben recuperar su cuenta con el proveedor");
        }
        PasswordResetToken reset = new PasswordResetToken();
        reset.setToken(UUID.randomUUID().toString());
        reset.setExpiration(LocalDateTime.now().plusMinutes(passwordResetDurationMinutes));
        reset.setUser(user);
        reset.setUsed(false);
        passwordResetTokenRepository.save(reset);
        notificationService.sendEmail(user.getEmail(), "Recuperación de contraseña",
                "Use este token antes de " + reset.getExpiration() + ": " + reset.getToken());
    }

    public void resetPassword(PasswordResetRequestDTO dto) {
        PasswordResetToken reset = passwordResetTokenRepository
                .findByTokenAndUsedFalseAndExpirationAfter(dto.token(), LocalDateTime.now())
                .orElseThrow(this::unauthorized);
        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        sessionRepository.deleteAllByUserId(user.getId());
        reset.setUsed(true);
        passwordResetTokenRepository.save(reset);
    }

    public void logout(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    public void setTwoFactor(Long userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(this::unauthorized);
        if (!"LOCAL".equals(user.getAuthProvider())) {
            throw new ApplicationException(ErrorCase.INVALID_OPERATION,
                    "El segundo factor interno solo aplica a usuarios locales");
        }
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
    }

    private AuthResponseDTO createTwoFactorChallenge(User user) {
        String challengeToken = UUID.randomUUID().toString();
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        Session session = new Session();
        session.setToken(challengeToken);
        session.setExpiration(LocalDateTime.now().plusMinutes(twoFactorDurationMinutes));
        session.setCode2FAHash(passwordEncoder.encode(code));
        session.setCode2FAExpiration(session.getExpiration());
        session.setUser(user);
        session.setActive(false);
        sessionRepository.save(session);
        notificationService.sendEmail(user.getEmail(), "Código de autenticación",
                "Su código de autenticación es: " + code);
        return AuthResponseDTO.challenge(challengeToken,
                session.getExpiration().toInstant(ZoneOffset.UTC));
    }

    private AuthResponseDTO createActiveSession(User user) {
        AuthResponseDTO response = createTokenResponse(user);
        Session session = new Session();
        session.setToken(response.accessToken());
        session.setExpiration(response.expiresAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
        session.setUser(user);
        session.setActive(true);
        sessionRepository.save(session);
        return response;
    }

    private AuthResponseDTO createTokenResponse(User user) {
        return jwtService.generateToken(user);
    }

    private AuthResponseDTO userResponse(User user) {
        return createActiveSession(user);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(
                ErrorCase.UNAUTHORIZED,
                "Credenciales incorrectas");
    }
}
