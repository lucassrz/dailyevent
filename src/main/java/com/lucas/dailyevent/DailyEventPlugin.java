package com.lucas.dailyevent;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DailyEventPlugin extends JavaPlugin {

    private static DailyEventPlugin instance;
    private SeasonManager seasonManager;

    public static DailyEventPlugin getInstance() {
        return instance;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.seasonManager = new SeasonManager(this);
        this.seasonManager.loadFromConfig();
        this.seasonManager.startDailyScheduler();

        Bukkit.getPluginManager().registerEvents(new GlobalListener(this.seasonManager), this);

        this.getCommand("season").setExecutor(new SeasonCommand(this.seasonManager));
    }

    @Override
    public void onDisable() {
        if (this.seasonManager != null) {
            this.seasonManager.saveToConfig();
        }
    }
}


