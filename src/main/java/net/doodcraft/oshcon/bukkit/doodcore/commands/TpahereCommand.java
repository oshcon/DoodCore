package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.TpaTimeoutTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpahereCommand implements CommandExecutor {

    public static Map<UUID, UUID> requesting = new ConcurrentHashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tpahere")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.tpahere", true)) {
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

                if (TpaCommand.requesting.containsKey(((Player) sender).getUniqueId())) {
                    sender.sendMessage("§7You already have a pending tpa request.\n§7Wait for it to timeout or use §b/tpcancel");
                    return false;
                }

                if (TpahereCommand.requesting.containsKey(((Player) sender).getUniqueId())) {
                    sender.sendMessage("§7You already have a pending tpahere request.\n§7Wait for it to timeout or use §b/tpcancel");
                    return false;
                }

                if (args.length <= 0) {
                    sender.sendMessage("§cPlease supply a player name.\n§7Example: §b/tpahere Dooder07");
                    return false;
                }

                if (CorePlayer.getPlayer(args[0]) == null) {
                    sender.sendMessage("§cThat player could not be found.");
                    return false;
                }

                if (CorePlayer.getPlayer(args[0]).equals(player)) {
                    sender.sendMessage("§cYou cannot teleport to yourself.");
                    return false;
                }

                // player blah sent you a tpa request
                CorePlayer.getPlayer(args[0]).sendMessage("§7" + sender.getName() + " would like you to teleport to them.\n  §7Accept with §b/tpaccept\n  §7Deny with §b/tpdeny");

                // you sent player blah a tpa request
                sender.sendMessage("§7You sent " + CorePlayer.getPlayer(args[0]).getName() + " a tpahere request.");

                // add them to the maps
                TpahereCommand.requesting.put(((Player) sender).getUniqueId(), CorePlayer.getPlayer(args[0]).getUniqueId());

                // new tpa timeout task
                new TpaTimeoutTask(player.getUniqueId(), 600L);
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}