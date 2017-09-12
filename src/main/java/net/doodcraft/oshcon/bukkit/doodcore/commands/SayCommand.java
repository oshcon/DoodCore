package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordMessages;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("say")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (!PlayerMethods.hasPermission(player, "core.command.say", true)) {
                    return false;
                }

                if (cPlayer != null) {
                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (args.length >= 1) {
                                Bukkit.broadcastMessage(Messages.parse(cPlayer, "§8[§7<nick>§8] §b" + Joiner.on(" ").join(args)));
                                DiscordMessages.sendGameSay(player, Joiner.on(" ").join(args));
                            } else {
                                sender.sendMessage("§7Broadcast a message.");
                                sender.sendMessage("§7Usage: §b/say The server will be restarting in 5m.");
                            }
                        }
                    }, 1L);
                }

                return true;
            } else {

                Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (args.length >= 1) {
                            Bukkit.broadcastMessage("§8[§dCONSOLE§8] §b" + Joiner.on(" ").join(args));
                            DiscordMessages.sendGameSay(null, Joiner.on(" ").join(args));
                        } else {
                            sender.sendMessage("§7Broadcast a message.");
                            sender.sendMessage("§7Usage: §b/say The server will be restarting in 5m.");
                        }
                    }
                }, 1L);
                return false;
            }
        }
        return false;
    }
}
