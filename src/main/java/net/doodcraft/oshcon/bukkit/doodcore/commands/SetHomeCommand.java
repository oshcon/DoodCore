package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sethome")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.sethome", true)) {
                    return false;
                }

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                StaticMethods.log("Saving " + StaticMethods.getPreciseLocString(player.getLocation()) + " for " + player.getName());

                if (args.length <= 0) {
                    cPlayer.addHome("home", player.getLocation());
                    return true;
                } else {
                    if (args[0].equalsIgnoreCase("home")) {
                        args[0] = "home";
                    }
                    cPlayer.addHome(args[0], player.getLocation());
                    return true;
                }
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}