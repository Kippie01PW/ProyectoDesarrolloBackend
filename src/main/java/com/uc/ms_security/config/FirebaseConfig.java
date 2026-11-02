package com.uc.ms_security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(
        name = "firebase.enabled",
        havingValue = "true"
)
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    FirebaseApp firebaseApp(
            @Value("${firebase.service-account-json}") String serviceAccountJson,
            @Value("${firebase.service-account-file:}") String serviceAccountFile)
            throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        InputStream credentials;
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            credentials = new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        } else if (serviceAccountFile != null && !serviceAccountFile.isBlank()) {
            credentials = Files.newInputStream(Path.of(serviceAccountFile));
        } else {
            throw new IllegalStateException(
                    "Firebase está habilitado pero no se configuró "
                            + "FIREBASE_SERVICE_ACCOUNT_JSON o FIREBASE_SERVICE_ACCOUNT_FILE");
        }
        FirebaseOptions options;
        try (InputStream input = credentials) {
            options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(input))
                    .build();
        }
        FirebaseApp app = FirebaseApp.initializeApp(options);
        logger.info("Firebase Admin SDK inicializado para el proyecto {}", options.getProjectId());
        return app;
    }
}
