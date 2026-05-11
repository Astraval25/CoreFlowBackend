package com.astraval.coreflow.main_modules.expense;

import java.util.List;

public final class ExpenseAccountTypes {

    public static final List<String> ALLOWED_TYPES = List.of(
            "Asset",
            "Other Asset",
            "Other Current Asset",
            "Fixed Asset",
            "Intangible Asset",
            "Non Current Asset",
            "Liability",
            "Other Current Liability",
            "Non Current Liability",
            "Other Liability",
            "Expense",
            "Cost Of Goods Sold",
            "Other Expense");

    private ExpenseAccountTypes() {
    }

    public static String normalize(String accountType) {
        if (accountType == null) {
            return null;
        }
        String requestedType = accountType.trim();
        return ALLOWED_TYPES.stream()
                .filter(type -> type.equalsIgnoreCase(requestedType))
                .findFirst()
                .orElse(null);
    }

    public static String validTypesMessage() {
        return "Valid account types are: " + String.join(", ", ALLOWED_TYPES);
    }
}
