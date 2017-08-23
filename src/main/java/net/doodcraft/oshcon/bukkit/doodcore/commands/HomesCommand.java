package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("homes")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.homes", true)) {
                    return false;
                }

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (cPlayer.getHomes().size() >= 1) {
                    player.sendMessage("§7Your homes: ");
                    player.sendMessage("§b" + Joiner.on("§7, §b").join(cPlayer.getHomes().keySet()));
                    return true;
                } else {
                    player.sendMessage("§cYou do not have any homes.");
                    return false;
                }
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}
