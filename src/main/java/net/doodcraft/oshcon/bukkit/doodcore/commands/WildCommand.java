package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WildernessSearchTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.Lag;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WildCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("wild")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.wild", true)) {
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

                player.sendMessage(StaticMethods.addColor("&7Preparing to teleport you into the wilderness, do not move..."));
                new WildernessSearchTask(player, 0).runTask(DoodCorePlugin.plugin);
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}