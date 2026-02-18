package com.astraval.coreflow.modules.advertisement;

import java.util.Set;

public class AdPlacement {

    public static final String DASHBOARD_ADS = "DASHBOARD_ADS";
    public static final String ORDER_PAGE_ADS = "ORDER_PAGE_ADS";

    private static final Set<String> SUPPORTED_PLACEMENTS = Set.of(
            DASHBOARD_ADS,
            ORDER_PAGE_ADS);

    public static String getDashboardAds() {
        return DASHBOARD_ADS;
    }

    public static String getOrderPageAds() {
        return ORDER_PAGE_ADS;
    }

    public static boolean isSupportedPlacement(String placement) {
        return normalizePlacement(placement) != null;
    }

    public static String normalizePlacement(String placement) {
        if (placement == null || placement.isBlank()) {
            return null;
        }
        String normalized = placement.trim().toUpperCase();
        return SUPPORTED_PLACEMENTS.contains(normalized) ? normalized : null;
    }
}
