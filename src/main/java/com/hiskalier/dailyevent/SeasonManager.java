package com.hiskalier.dailyevent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class SeasonManager {

    private final DailyEventPlugin plugin;
    private Season currentSeason;
    private LocalDate lastChangeDate;
    private final Random random;
    private BukkitTask schedulerTask;
    private LocalizationManager localizationManager;

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
        this.rotationMode = RotationMode.IN_GAME_TIME; // Default to in-game time
        this.rotationMinutes = 24L * 60L; // default 1 day
        this.inGameChangeTimeTicks = 18000L; // default to midnight (12:00 AM)
        this.lastChangeEpochMillis = System.currentTimeMillis();
        this.enabledSeasons = new ArrayList<>();
        this.localizationManager = plugin.getLocalizationManager();
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public void setSeason(Season season) {
        this.currentSeason = season;
        saveToConfig();
        Bukkit.getLogger().info("[DailyEvent] Season set to " + season);
    }

    public void loadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        String seasonStr = cfg.getString("currentSeason", "BLOOD");
        String dateStr = cfg.getString("lastChangeDate", LocalDate.now().toString());
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
        cfg.set("rotation.mode", this.rotationMode.name());
        cfg.set("rotation.minutes", this.rotationMinutes);
        cfg.set("rotation.inGameChangeTime", this.inGameChangeTimeTicks);
        cfg.set("rotation.lastChangeEpochMillis", this.lastChangeEpochMillis);
        plugin.saveConfig();
    }
    
    public void saveCurrentSeasonOnly() {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set("currentSeason", this.currentSeason.name());
        cfg.set("lastChangeDate", this.lastChangeDate.toString());
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
                
                // Check if we're at the target time (with a small window)
                boolean inWindow = isWithinWindow(time, inGameChangeTimeTicks, 100L);
                if (inWindow && !today.equals(lastChangeDate)) {
                    performSeasonChange();
                    lastChangeDate = today;
                    lastChangeEpochMillis = System.currentTimeMillis();
                    // Only save the current season, not the entire config
                    saveCurrentSeasonOnly();
                }
            }
        }, 20L, 20L * 10L); // check every 10 seconds for more precision
    }

    private boolean isWithinWindow(long current, long target, long window) {
        // Handle the case where we're near the target time
        // Minecraft time: 0 = 6:00 AM, 6000 = 12:00 PM, 12000 = 6:00 PM, 18000 = 12:00 AM
        
        // Check if current time is within the window around target time
        if (current >= target - window && current <= target + window) {
            return true;
        }
        
        // Handle wrap-around case (when target is near 0 and current is near 24000)
        if (target < window) {
            // Target is early in the day, check if current is late in the previous day
            long wrapAroundStart = 24000 - window + target;
            if (current >= wrapAroundStart) {
                return true;
            }
        }
        
        // Handle wrap-around case (when target is near 24000 and current is near 0)
        if (target > 24000 - window) {
            // Target is late in the day, check if current is early in the next day
            long wrapAroundEnd = target - 24000 + window;
            if (current <= wrapAroundEnd) {
                return true;
            }
        }
        
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
        
        // Message dans la title bar pour tous les joueurs
        String seasonName = localizationManager.getSeasonName(currentSeason);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("season", seasonName);
        String message = localizationManager.getMessage("season_change", placeholders);
        
        // Envoyer le message dans la title bar
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitleBar(player, message);
        }
        
        // Message dans le chat aussi
        Bukkit.broadcastMessage(message);
    }
    
    private void sendTitleBar(Player player, String message) {
        try {
            // Utiliser l'API Bukkit pour la title bar (plus compatible)
            player.sendTitle(message, "", 10, 40, 10);
        } catch (Exception e) {
            // Fallback vers le chat si la title bar échoue
            player.sendMessage(message);
        }
    }
}


