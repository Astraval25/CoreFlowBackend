package com.astraval.coreflow.modules.subscription;

public class SubscriptionStatus {

    public static final String TRIAL = "TRIAL";
    public static final String ACTIVE = "ACTIVE";
    public static final String PAST_DUE = "PAST_DUE";
    public static final String CANCELED = "CANCELED";
    public static final String EXPIRED = "EXPIRED";

    public static String getTrial() {
        return TRIAL;
    }

    public static String getActive() {
        return ACTIVE;
    }

    public static String getPastDue() {
        return PAST_DUE;
    }

    public static String getCanceled() {
        return CANCELED;
    }

    public static String getExpired() {
        return EXPIRED;
    }

    private SubscriptionStatus() {
    }
}
