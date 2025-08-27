package com.lucas.dailyevent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SeasonCommand implements CommandExecutor {

    private final SeasonManager seasonManager;

    public SeasonCommand(SeasonManager seasonManager) {
        this.seasonManager = seasonManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6Season actuelle: §e" + seasonManager.getCurrentSeason() + (seasonManager.isBloodMoonTonight() ? " §c(Blood Moon)" : ""));
            return true;
        }

        if (!sender.hasPermission("dailyevent.admin")) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args.length >= 2) {
            Season s = Season.fromString(args[1], null);
            if (s == null) {
                sender.sendMessage("§cSaison invalide.");
                return true;
            }
            seasonManager.setSeason(s);
            sender.sendMessage("§aSaison forcée: §e" + s);
            return true;
        }

        if (args[0].equalsIgnoreCase("blood")) {
            boolean state = !seasonManager.isBloodMoonTonight();
            seasonManager.setBloodMoonTonight(state);
            sender.sendMessage("§aBlood Moon: " + (state ? "§cON" : "§7OFF"));
            return true;
        }

        sender.sendMessage("§e/season §7- voir la saison\n§e/season set <HOSTILE|NOCTURNE|ABUNDANCE|FAMINE|FUN> §7- forcer\n§e/season blood §7- toggle Blood Moon");
        return true;
    }
}


