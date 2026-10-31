package com.uc.ms_security.authorization;

import com.uc.ms_security.service.PermissionService;
import com.uc.ms_security.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class DynamicAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final PermissionService permissionService;
    private final SessionRepository sessionRepository;

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            RequestAuthorizationContext context) {

        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        HttpServletRequest request = context.getRequest();
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")
                || sessionRepository.findByTokenAndActiveTrueAndExpirationAfter(
                header.substring(7), LocalDateTime.now()).isEmpty()) {
            return new AuthorizationDecision(false);
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            return new AuthorizationDecision(false);
        }

        boolean permitted = permissionService.hasPermission(
                userId,
                request.getMethod(),
                request.getRequestURI()
        );

        return new AuthorizationDecision(permitted);
    }
}
