package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.Lag;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("home")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.home", true)) {
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

                if (WarmupTeleportTask.teleporting.contains(player.getUniqueId())) {
                    player.sendMessage(StaticMethods.addColor("&cYou are already being teleported, please wait."));
                    return false;
                }

                if (Lag.getTPS() <= 15) {
                    player.sendMessage(StaticMethods.addColor("&cThis feature has been temporarily disabled for your safety. Try again in a few minutes."));
                    return false;
                }

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (cPlayer.getHomes().size() <= 0) {
                    player.sendMessage("§cYou do not have a home.");
                    return false;
                }

                if (args.length <= 0) {
                    if (cPlayer.getHomes().containsKey("home")) {
                        // teleport to "home"
                        new WarmupTeleportTask(player, StaticMethods.getPreciseLocationFromString(cPlayer.getHomes().get("home")), null, "§7Welcome home!", "home", 5000);
                        return true;
                    }

                    player.sendMessage("§cPlease supply a home name.");
                    player.sendMessage("§7Your homes: ");
                    player.sendMessage("§b" + Joiner.on("§7, §b").join(cPlayer.getHomes().keySet()));
                    return true;
                }

                for (String id : cPlayer.getHomes().keySet()) {
                    if (id.equalsIgnoreCase(args[0])) {
                        // teleport to args[0]
                        new WarmupTeleportTask(player, StaticMethods.getPreciseLocationFromString(cPlayer.getHomes().get(id)), null, "§7You are now at §b" + id + "§7.", "home", 5000);
                        return true;
                    }
                }

                // Cant find a home by that name
                player.sendMessage("§cCannot find a home by that name.");
                player.sendMessage("§7Your homes: ");
                player.sendMessage("§b" + Joiner.on("§7, §b").join(cPlayer.getHomes().keySet()));
                return false;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}