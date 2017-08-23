package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class BackCommand implements CommandExecutor {

    public static HashMap<UUID, Location> deathLocations = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("back")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.back", true)) {
                    return false;
                }

                if (!player.hasPermission("core.bypass.vote")) {
                    if (!PlayerMethods.hasVotedToday(player)) {
                        if (!PlayerMethods.isSupporter(player)) {
                            player.sendMessage("§cUnlock this command for 24 hours by voting.");
                            player.sendMessage("§7See all the eligible voting sites using §b/vote");
                            return false;
                        }
                    }
                }

                if (deathLocations.containsKey(player.getUniqueId())) {
                    player.sendMessage("§7Teleporting you to your last death location, do not move...");
                    new WarmupTeleportTask(player, deathLocations.get(player.getUniqueId()), null, "§7You've been teleported to where you last died.", "back", 5000);
                    return true;
                }

                sender.sendMessage("§cYou haven't died; you have nowhere to go back to.");
                return false;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }

    public static void addBackLocation(Player player) {
        deathLocations.put(player.getUniqueId(), player.getLocation());
        player.sendMessage("§7Your death location: §8[§e" + StaticMethods.getRoundedLocString(player.getLocation()) + "§8]");
        if (player.hasPermission("core.command.back")) {
            player.sendMessage("§7You can teleport there using §b/back");
        }
    }

    public static void dumpDeathLocations() {
        Configuration locations = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "death-locations.yml");
        locations.delete();
        locations.save();
        for (UUID uuid : deathLocations.keySet()) {
            locations.add(uuid.toString(), StaticMethods.getPreciseLocString(deathLocations.get(uuid)));
        }
        locations.save();
        deathLocations.clear();
    }

    public static void loadDeathLocations() {
        Configuration locations = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "death-locations.yml");
        for (String uuid : locations.getKeys(false)) {
            deathLocations.put(UUID.fromString(uuid), StaticMethods.getPreciseLocationFromString(locations.getString(uuid)));
        }
        locations.delete();
    }
}