package com.astraval.coreflow.common.scheduler;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("cronJobConfig")
@RequiredArgsConstructor
public class CronJobConfigResolver {

    private final CronJobProperties properties;

    public String cron(String jobKey) {
        CronJobProperties.JobConfig job = properties.getJobs().get(jobKey);
        if (job == null || job.getCron() == null || job.getCron().isBlank()) {
            throw new IllegalStateException("Missing cron configuration for job key: " + jobKey);
        }
        return job.getCron();
    }

    public String zone(String jobKey) {
        CronJobProperties.JobConfig job = properties.getJobs().get(jobKey);
        if (job != null && job.getZone() != null && !job.getZone().isBlank()) {
            return job.getZone();
        }
        if (properties.getDefaultZone() != null && !properties.getDefaultZone().isBlank()) {
            return properties.getDefaultZone();
        }
        return "Asia/Kolkata";
    }
}
