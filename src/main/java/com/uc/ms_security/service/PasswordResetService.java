package com.uc.ms_security.service;

import com.uc.ms_security.dto.account.ForgotPasswordRequestDTO;
import com.uc.ms_security.entity.PasswordResetToken;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.repository.PasswordResetTokenRepository;
import com.uc.ms_security.repository.SessionRepository;
import com.uc.ms_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.uc.ms_security.dto.account.ResetPasswordRequestDTO;
import com.uc.ms_security.exception.ErrorCase;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.password-reset-duration-minutes}")
    private long resetDurationMinutes;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        // 1. Si el usuario no existe o no es local, terminamos sin avisar (siempre 204)
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user == null || !"LOCAL".equals(user.getAuthProvider())) {
            return;
        }
        // TODO HU-01: cuando exista el campo active, agregar  || !user.isActive()

        // 2. Invalidar los tokens anteriores que sigan sin usar
        List<PasswordResetToken> previousTokens =
                passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId());
        for (PasswordResetToken previous : previousTokens) {
            previous.setUsed(true);
        }
        passwordResetTokenRepository.saveAll(previousTokens);

        // 3. Crear el token nuevo, que vence en 30 minutos
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(resetDurationMinutes));
        passwordResetTokenRepository.save(resetToken);

        // 4. Enviar el correo con el enlace al front
        String link = resetPasswordUrl + "?token=" + resetToken.getToken();
        try {
            notificationService.sendEmail(
                    user.getEmail(),
                    "Recuperación de contraseña",
                    "Haz clic en el enlace para crear una nueva contraseña (vence en "
                            + resetDurationMinutes + " minutos): "
                            + "<a href=\"" + link + "\">Restablecer contraseña</a>");
        } catch (ApplicationException exception) {
            logger.warn("No se pudo enviar el correo de recuperación al usuario {}", user.getId());
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        // 1. ¿Existe el token?
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.BAD_REQUEST,
                        "El enlace de recuperación no es válido."));

        // 2. ¿Ya se usó?
        if (resetToken.isUsed()) {
            throw new ApplicationException(
                    ErrorCase.BAD_REQUEST,
                    "El enlace de recuperación ya fue utilizado.");
        }

        // 3. ¿Venció? Solo sirve si la expiración es POSTERIOR a este instante
        if (!resetToken.getExpiration().isAfter(LocalDateTime.now())) {
            throw new ApplicationException(
                    ErrorCase.BAD_REQUEST,
                    "El enlace de recuperación expiró.");
        }

        // 4. Cambiar la contraseña (cifrada con BCrypt)
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        // 5. Marcar el token como usado (uso único)
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // 6. Cerrar todas las sesiones: sus JWT dejan de servir
        sessionRepository.deleteAllByUserId(user.getId());
    }

}
