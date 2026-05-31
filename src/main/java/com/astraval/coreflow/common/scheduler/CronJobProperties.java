package com.astraval.coreflow.common.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cron")
public class CronJobProperties {

    private String defaultZone = "Asia/Kolkata";
    private Map<String, JobConfig> jobs = new HashMap<>();

    @Getter
    @Setter
    public static class JobConfig {
        private String cron;
        private String zone;
    }
}
