package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("me")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());

                if (cPlayer != null) {
                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (args.length >= 1) {
                                Bukkit.broadcastMessage(Messages.parse(cPlayer, "§e* <nick> §e" + Joiner.on(" ").join(args)));
                                DiscordManager.sendGameMe(player, Joiner.on(" ").join(args));
                            } else {
                                sender.sendMessage("§7Broadcast an emotion.");
                                sender.sendMessage("§7Usage: §b/me scratches head in confusion");
                            }
                        }
                    },1L);
                }

                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}