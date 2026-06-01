package com.astraval.coreflow.main_modules.items;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UnitType {
    KG,
    LITER,
    INCH,
    PCS,
    METER,
    GRAM,
    MILLILITER;

    @JsonCreator
    public static UnitType fromValue(String raw) {
        if (raw == null) {
            return null;
        }

        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) {
            return null;
        }

        return switch (value) {
            case "UNIT", "PCE", "PC", "PIECE", "PIECES" -> PCS;
            case "ML", "MILLI_LITER", "MILLI-LITER" -> MILLILITER;
            case "L", "LTR" -> LITER;
            case "G", "GM" -> GRAM;
            case "M", "MTR" -> METER;
            case "IN" -> INCH;
            default -> UnitType.valueOf(value);
        };
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
