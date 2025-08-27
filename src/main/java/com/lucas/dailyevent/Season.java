package com.lucas.dailyevent;

public enum Season {
    BLOOD,
    NOCTURNE,
    FAMINE,
    STORM,
    TENEBRE;

    public static Season fromString(String value, Season fallback) {
        if (value == null) return fallback;
        try {
            return Season.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}


