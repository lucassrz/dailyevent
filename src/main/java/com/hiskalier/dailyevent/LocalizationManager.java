package com.hiskalier.dailyevent;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {
    
    private final DailyEventPlugin plugin;
    private Map<String, String> messages;
    private Map<String, String> seasonNames;
    
    public LocalizationManager(DailyEventPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.seasonNames = new HashMap<>();
        loadLocalization();
    }
    
    public void loadLocalization() {
        FileConfiguration config = plugin.getConfig();
        
        // Charger les messages
        this.messages.clear();
        this.messages.put("season_change", config.getString("messages.season_change", "§6New season: §e{season}"));
        this.messages.put("season_current", config.getString("messages.season_current", "§6Current season: §e{season}"));
        
        // Charger les noms des saisons
        this.seasonNames.clear();
        for (String season : config.getConfigurationSection("seasons.names").getKeys(false)) {
            this.seasonNames.put(season, config.getString("seasons.names." + season, season));
        }
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, key);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    public String getSeasonName(Season season) {
        return seasonNames.getOrDefault(season.name(), season.name());
    }
    
    public void reloadLocalization() {
        loadLocalization();
    }
}
