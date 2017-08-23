package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.Lag;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("spawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.spawn", true)) {
                    return false;
                }

                if (WarmupTeleportTask.teleporting.contains(player.getUniqueId())) {
                    player.sendMessage(StaticMethods.addColor("&cYou are already being teleported, please wait."));
                    return false;
                }

                if (Lag.getTPS() <= 15) {
                    player.sendMessage(StaticMethods.addColor("&cThis feature has been temporarily disabled for your safety. Try again in a few minutes."));
                    return false;
                }

                player.sendMessage(StaticMethods.addColor("&7Preparing to teleport you to spawn, do not move..."));
                CommandCooldowns.addCooldown(player.getUniqueId(), "spawn", 60000L);
                new WarmupTeleportTask(player, Bukkit.getWorlds().get(0).getSpawnLocation(), null, "§7You are now at spawn.", "spawn", 5000);
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}