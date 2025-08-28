package com.hiskalier.dailyevent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class SeasonManager {

    private final DailyEventPlugin plugin;
    private Season currentSeason;
    private final Random random;
    private BukkitTask schedulerTask;
    private LocalizationManager localizationManager;

    private long inGameChangeTimeTicks; // 0..23999
    private long lastCheckedTime; // Track last checked time to detect crossing

    private List<Season> enabledSeasons;

    public SeasonManager(DailyEventPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
        // Seasons always change at precise in-game time
        this.inGameChangeTimeTicks = 18000L; // default to midnight (12:00 AM)
        this.lastCheckedTime = 0L; // Initialize last checked time
        this.enabledSeasons = new ArrayList<>();
        this.localizationManager = plugin.getLocalizationManager();
        // Season will be set randomly in loadFromConfig()
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public void setSeason(Season season) {
        this.currentSeason = season;
        // No automatic saving - seasons change in memory only
        Bukkit.getLogger().info("[DailyEvent] Season set to " + season);
    }

    public void loadFromConfig() {
        FileConfiguration cfg = plugin.getConfig();
        
        // Load only the enabled seasons list from config
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
        
        // Load the in-game time when seasons change
        this.inGameChangeTimeTicks = cfg.getLong("inGameChangeTime", 18000L); // Default to midnight
        
        // Initialize last checked time to current world time
        if (!Bukkit.getWorlds().isEmpty()) {
            this.lastCheckedTime = Bukkit.getWorlds().get(0).getTime();
        } else {
            this.lastCheckedTime = 0L;
        }
        
        // Generate a random season at startup
        if (!enabledSeasons.isEmpty()) {
            this.currentSeason = enabledSeasons.get(random.nextInt(enabledSeasons.size()));
        } else {
            this.currentSeason = Season.BLOOD; // Fallback if no seasons enabled
        }
        this.enabledSeasons = parsed;
    }


    


    public void startDailyScheduler() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }

        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Check primary world time for season change
            if (Bukkit.getWorlds().isEmpty()) return;
            long time = Bukkit.getWorlds().get(0).getTime();
            
            // Check if we've crossed the target time threshold
            // This handles both forward and backward time changes
            if (hasCrossedThreshold(lastCheckedTime, time, inGameChangeTimeTicks)) {
                performSeasonChange();
            }
            
            // Update last checked time
            lastCheckedTime = time;
        }, 20L, 20L * 5L); // check every 5 seconds for maximum precision
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
    
    /**
     * Check if the time has crossed the threshold between two checks
     * This handles both forward and backward time changes (like /time set)
     */
    private boolean hasCrossedThreshold(long previousTime, long currentTime, long threshold) {
        // Handle normal forward progression
        if (previousTime < threshold && currentTime >= threshold) {
            return true;
        }
        
        // Handle backward time changes (like /time set)
        if (previousTime > threshold && currentTime <= threshold) {
            return true;
        }
        
        // Handle day wrap-around (when time goes from 23999 to 0)
        if (previousTime > threshold && currentTime < threshold && 
            Math.abs(previousTime - currentTime) > 1000) { // Significant jump
            return true;
        }
        
        // Handle reverse day wrap-around (when time goes from 0 to 23999)
        if (previousTime < threshold && currentTime > threshold && 
            Math.abs(previousTime - currentTime) > 1000) { // Significant jump
            return true;
        }
        
        return false;
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


