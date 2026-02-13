package com.astraval.coreflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private boolean enabled;
    private String projectId;
    private Credentials credentials = new Credentials();

    @Data
    public static class Credentials {
        private String filePath;
        private String json;
    }
}
