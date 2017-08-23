package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpcancelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tpcancel")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.tpcancel", true)) {
                    return false;
                }

                if (TpaCommand.requesting.containsKey(((Player) sender).getUniqueId())) {
                    sender.sendMessage("§7You cancelled your tpa request.");
                    Bukkit.getPlayer(TpaCommand.requesting.get(((Player) sender).getUniqueId())).sendMessage("§7" + sender.getName() + " cancelled their tpa request.");
                    TpaCommand.requesting.remove(((Player) sender).getUniqueId());
                    return false;
                }

                if (TpahereCommand.requesting.containsKey(((Player) sender).getUniqueId())) {
                    sender.sendMessage("§7You cancelled your tpahere request.");
                    Bukkit.getPlayer(TpahereCommand.requesting.get(((Player) sender).getUniqueId())).sendMessage("§7" + sender.getName() + " cancelled their tpahere request.");
                    TpahereCommand.requesting.remove(((Player) sender).getUniqueId());
                    return false;
                }

                sender.sendMessage("§cYou have no requests to cancel.");
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}
