package com.lucas.dailyevent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeasonManager {

    private final DailyEventPlugin plugin;
    private Season currentSeason;
    private LocalDate lastChangeDate;
    private boolean bloodMoonTonight;
    private final Random random;
    private BukkitTask schedulerTask;

    public enum RotationMode { MINUTES, IN_GAME_TIME }

    private RotationMode rotationMode;
    private long rotationMinutes;
    private long inGameChangeTimeTicks; // 0..23999
    private long lastChangeEpochMillis;
    private List<Season> enabledSeasons;

    public SeasonManager(DailyEventPlugin plugin) {
        this.plugin = plugin;
        this.currentSeason = Season.BLOOD;
        this.lastChangeDate = LocalDate.now();
        this.random = new Random();
        this.bloodMoonTonight = false;
        this.rotationMode = RotationMode.MINUTES;
        this.rotationMinutes = 24L * 60L; // default 1 day
        this.inGameChangeTimeTicks = 0L; // midnight
        this.lastChangeEpochMillis = System.currentTimeMillis();
        this.enabledSeasons = new ArrayList<>();
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public boolean isBloodMoonTonight() {
        return bloodMoonTonight;
    }

    public void setBloodMoonTonight(boolean bloodMoonTonight) {
        this.bloodMoonTonight = bloodMoonTonight;
        saveToConfig();
    }

    public void setSeason(Season season) {
        this.currentSeason = season;
        saveToConfig();
        Bukkit.getLogger().info("[DailyEvent] Season set to " + season);
    }

    public void loadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        String seasonStr = cfg.getString("currentSeason", "HOSTILE");
        String dateStr = cfg.getString("lastChangeDate", LocalDate.now().toString());
        boolean blood = cfg.getBoolean("bloodMoonTonight", false);
        String modeStr = cfg.getString("rotation.mode", "MINUTES");
        long minutes = cfg.getLong("rotation.minutes", 24L * 60L);
        long tickTime = cfg.getLong("rotation.inGameChangeTime", 0L);
        long lastMs = cfg.getLong("rotation.lastChangeEpochMillis", System.currentTimeMillis());

        List<String> enabled = cfg.getStringList("seasons.enabled");
        List<Season> parsed = new ArrayList<>();
        if (enabled != null && !enabled.isEmpty()) {
            for (String s : enabled) {
                Season val = Season.fromString(s, null);
                if (val != null) parsed.add(val);
            }
        }
        if (parsed.isEmpty()) {
            for (Season s : Season.values()) parsed.add(s);
        }

        this.currentSeason = Season.fromString(seasonStr, Season.BLOOD);
        this.lastChangeDate = LocalDate.parse(dateStr);
        this.bloodMoonTonight = blood;
        this.rotationMode = "IN_GAME_TIME".equalsIgnoreCase(modeStr) ? RotationMode.IN_GAME_TIME : RotationMode.MINUTES;
        this.rotationMinutes = Math.max(1L, minutes);
        this.inGameChangeTimeTicks = Math.max(0L, Math.min(23999L, tickTime));
        this.lastChangeEpochMillis = lastMs;
        this.enabledSeasons = parsed;
    }

    public void saveToConfig() {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("currentSeason", this.currentSeason.name());
        cfg.set("lastChangeDate", this.lastChangeDate.toString());
        cfg.set("bloodMoonTonight", this.bloodMoonTonight);
        cfg.set("rotation.mode", this.rotationMode.name());
        cfg.set("rotation.minutes", this.rotationMinutes);
        cfg.set("rotation.inGameChangeTime", this.inGameChangeTimeTicks);
        cfg.set("rotation.lastChangeEpochMillis", this.lastChangeEpochMillis);
        plugin.saveConfig();
    }

    public void startDailyScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }

        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (rotationMode == RotationMode.MINUTES) {
                long nowMs = System.currentTimeMillis();
                long periodMs = rotationMinutes * 60_000L;
                if (nowMs - lastChangeEpochMillis >= periodMs) {
                    performSeasonChange();
                    lastChangeEpochMillis = nowMs;
                    saveToConfig();
                }
            } else {
                // IN_GAME_TIME: check primary world time
                if (Bukkit.getWorlds().isEmpty()) return;
                long time = Bukkit.getWorlds().get(0).getTime();
                LocalDate today = LocalDate.now();
                boolean inWindow = isWithinWindow(time, inGameChangeTimeTicks, 120L);
                if (inWindow && !today.equals(lastChangeDate)) {
                    performSeasonChange();
                    lastChangeDate = today;
                    lastChangeEpochMillis = System.currentTimeMillis();
                    saveToConfig();
                }
            }
        }, 20L, 20L * 30L); // check every 30 seconds
    }

    private boolean isWithinWindow(long current, long target, long window) {
        if (current >= target && current <= target + window) return true;
        // wrap-around case near 24000
        long targetMinus = (target - window + 24000L) % 24000L;
        if (current >= targetMinus && current <= target) return true;
        return false;
    }

    private void rotateSeason() {
        if (enabledSeasons == null || enabledSeasons.isEmpty()) {
            enabledSeasons = new ArrayList<>();
            for (Season s : Season.values()) enabledSeasons.add(s);
        }
        if (enabledSeasons.size() == 1) {
            this.currentSeason = enabledSeasons.get(0);
            return;
        }
        Season next;
        do {
            next = enabledSeasons.get(random.nextInt(enabledSeasons.size()));
        } while (next == this.currentSeason);
        this.currentSeason = next;
    }

    private void performSeasonChange() {
        rotateSeason();
        lastChangeDate = LocalDate.now();
        bloodMoonTonight = false;
        if (currentSeason == Season.NOCTURNE) {
            // Blood Moon n'est plus une chance, c'est maintenant une saison à part entière
        bloodMoonTonight = false;
        }
        Bukkit.broadcastMessage("§6[DailyEvent] Nouvelle saison: §e" + currentSeason + (bloodMoonTonight ? " §c(Blood Moon possible)" : ""));
    }
}


