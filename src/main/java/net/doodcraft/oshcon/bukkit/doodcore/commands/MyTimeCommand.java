package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyTimeCommand implements CommandExecutor {

    // FIXME: Player times are not being preserved on server restart?

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("mytime")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (cPlayer != null) {
                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {

                            Long activeTime = cPlayer.getCurrentActiveTime();
                            Long afkTime = cPlayer.getCurrentAfkTime();

                            sender.sendMessage("§7Total Active Time: §b" + StaticMethods.getDurationBreakdown(activeTime));
                            sender.sendMessage("§7Total AFK Time: §b" + StaticMethods.getDurationBreakdown(afkTime));
                            sender.sendMessage("§7Total Online Time: §b" + StaticMethods.getDurationBreakdown(activeTime + afkTime));
                        }
                    }, 1L);
                }

                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                // TODO: Force dump of a players current active time, or fetch it, then print it
                return false;
            }
        }
        return false;
    }
}
