package com.hiskalier.dailyevent;

public enum Season {
    BLOOD,
    FAMINE,
    STORM,
    TENEBRE,
    ILLUSION,
    PARANOIA;

    public static Season fromString(String value, Season fallback) {
        if (value == null) return fallback;
        try {
            return Season.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }
}


