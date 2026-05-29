package com.astraval.coreflow.main_modules.companylink;

public final class ConnectionStatus {

    private ConnectionStatus() {}

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";

    public static boolean isPending(String status) {
        return PENDING.equals(status);
    }

    public static boolean isAccepted(String status) {
        return ACCEPTED.equals(status);
    }

    public static boolean isRejected(String status) {
        return REJECTED.equals(status);
    }

    /**
     * Returns true if orders/payments are allowed for the given connection status.
     * Allowed when: null (unlinked offline partner) or ACCEPTED.
     * Blocked when: PENDING or REJECTED.
     */
    public static boolean allowsTransactions(String status) {
        return status == null || ACCEPTED.equals(status);
    }
}
