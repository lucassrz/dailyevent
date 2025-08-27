package com.hiskalier.dailyevent;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyEventPlugin extends JavaPlugin {

    private static DailyEventPlugin instance;
    private SeasonManager seasonManager;
    private LocalizationManager localizationManager;

    public static DailyEventPlugin getInstance() {
        return instance;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // Only save default config if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        this.localizationManager = new LocalizationManager(this);
        this.seasonManager = new SeasonManager(this);
        this.seasonManager.loadFromConfig();
        this.seasonManager.startDailyScheduler();

        Bukkit.getPluginManager().registerEvents(new GlobalListener(this.seasonManager), this);

        this.getCommand("season").setExecutor(new SeasonCommand(this.seasonManager, this.localizationManager));
    }

    @Override
    public void onDisable() {
        // Plugin disabled - no automatic config saving
        // Seasons will be saved only when they change
    }
}


