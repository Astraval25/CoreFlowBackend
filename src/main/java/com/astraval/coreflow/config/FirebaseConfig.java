package com.astraval.coreflow.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    @Bean
    @ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp(FirebaseProperties firebaseProperties) throws IOException {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (!firebaseApps.isEmpty()) {
            return firebaseApps.get(0);
        }

        GoogleCredentials credentials = resolveCredentials(firebaseProperties);
        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder().setCredentials(credentials);

        if (StringUtils.hasText(firebaseProperties.getProjectId())) {
            optionsBuilder.setProjectId(firebaseProperties.getProjectId());
        }

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(optionsBuilder.build());
        log.info("Firebase initialized. projectId={}", firebaseProperties.getProjectId());
        return firebaseApp;
    }

    @Bean
    @ConditionalOnBean(FirebaseApp.class)
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private GoogleCredentials resolveCredentials(FirebaseProperties firebaseProperties) throws IOException {
        String inlineJson = firebaseProperties.getCredentials().getJson();
        if (StringUtils.hasText(inlineJson)) {
            try (InputStream inputStream = new ByteArrayInputStream(inlineJson.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        String configuredPath = firebaseProperties.getCredentials().getFilePath();
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        String pathToUse = StringUtils.hasText(configuredPath) ? configuredPath : envPath;

        if (StringUtils.hasText(pathToUse)) {
            Path credentialsPath = Path.of(pathToUse);
            if (!Files.exists(credentialsPath)) {
                throw new IOException("Firebase credentials file not found at: " + pathToUse);
            }
            try (InputStream inputStream = Files.newInputStream(credentialsPath)) {
                return GoogleCredentials.fromStream(inputStream);
            }
        }

        return GoogleCredentials.getApplicationDefault();
    }
}
