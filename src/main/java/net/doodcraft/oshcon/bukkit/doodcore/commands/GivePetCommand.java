package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.listeners.PlayerListener;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.GivePetTimeoutTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class GivePetCommand implements Listener, CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("givepet")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.givepet", true)) {
                    return false;
                }

                if (args.length <= 0) {
                    sender.sendMessage("§cPlease supply a player name.\n§7Example: §b/givepet " + sender.getName());
                    return false;
                }

                if (!Bukkit.getPlayer(args[0]).isOnline()) {
                    sender.sendMessage("§cThat player could not be found.");
                    return false;
                }

                Bukkit.getPlayer(args[0]).sendMessage("§7Someone is preparing to send you a pet.");
                sender.sendMessage("§7Now, find the pet you want to give and right click it.");
                PlayerListener.requesting.put(((Player) sender).getUniqueId(), Bukkit.getPlayer(args[0]).getUniqueId());
                PlayerListener.waiting.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
                new GivePetTimeoutTask(player);
                CommandCooldowns.addCooldown(((Player) sender).getUniqueId(), "givepet", 30000L);
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}