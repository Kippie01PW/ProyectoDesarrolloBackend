package com.uc.ms_security.service;

import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class NotificationService {

    private final RestClient restClient;

    public NotificationService(@Value("${notification.service.url}") String notificationUrl) {
        this.restClient = RestClient.builder().baseUrl(notificationUrl).build();
    }

    public void sendEmail(String recipient, String subject, String body) {
        try {
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("to", recipient, "text", "<h3>" + subject + "</h3><p>" + body + "</p>"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "No fue posible enviar el correo de notificación");
        }
    }
}
