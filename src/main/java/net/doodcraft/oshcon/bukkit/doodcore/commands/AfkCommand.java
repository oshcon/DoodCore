package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("afk")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());

                Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (!cPlayer.isCurrentlyAfk()) {
                            if (args.length >= 1) {
                                cPlayer.setAfkStatus(true, "§b" + Joiner.on(" ").join(args) + "§7");
                            } else {
                                cPlayer.setAfkStatus(true, "No Given Reason§7");
                            }
                        } else {
                            sender.sendMessage("§7You are already marked AFK.");
                        }
                    }
                },10L);

                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}