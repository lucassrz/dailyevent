package com.hiskalier.dailyevent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SeasonCommand implements CommandExecutor {

    private final SeasonManager seasonManager;
    private final LocalizationManager localizationManager;

    public SeasonCommand(SeasonManager seasonManager, LocalizationManager localizationManager) {
        this.seasonManager = seasonManager;
        this.localizationManager = localizationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("season", localizationManager.getSeasonName(seasonManager.getCurrentSeason()));
            sender.sendMessage(localizationManager.getMessage("season_current", placeholders));
            return true;
        }

        if (!sender.hasPermission("dailyevent.admin")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args.length >= 2) {
            Season s = Season.fromString(args[1], null);
            if (s == null) {
                sender.sendMessage("§cInvalid season.");
                return true;
            }
            seasonManager.setSeason(s);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("season", localizationManager.getSeasonName(s));
            sender.sendMessage("§aSeason forced to: §e" + localizationManager.getSeasonName(s));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            localizationManager.reloadLocalization();
            sender.sendMessage("§aConfiguration reloaded!");
            return true;
        }

        sender.sendMessage("§e/season §7- see current season\n§e/season set <BLOOD|FAMINE|STORM|TENEBRE|ILLUSION|PARANOIA> §7- force season\n§e/season reload §7- reload localization");
        return true;
    }
}


