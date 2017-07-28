package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SudoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sudo")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());

                if (!PlayerMethods.hasPermission(player, "core.command.sudo", true)) {
                    return false;
                }

                if (cPlayer != null) {
                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (args.length >= 1) {
                                List<String> msg = new ArrayList<>(Arrays.asList(args));
                                msg.remove(args[0]);

                                if (Bukkit.getPlayer(args[0]) != null) {
                                    Bukkit.getPlayer(args[0]).chat(Joiner.on(" ").join(msg));
                                }

                                if (args[0].equalsIgnoreCase("*")) {
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        player.chat(Joiner.on(" ").join(msg));
                                    }
                                }
                            } else {
                                sender.sendMessage("§7Force a player to run a command or chat.");
                                sender.sendMessage("§7Usage: §b/sudo Dooder07 /restart");
                            }
                        }
                    },1L);
                }

                return true;
            } else {
                Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (args.length >= 1) {
                            List<String> msg = new ArrayList<>(Arrays.asList(args));
                            msg.remove(args[0]);

                            if (Bukkit.getPlayer(args[0]) != null) {
                                Bukkit.getPlayer(args[0]).chat(Joiner.on(" ").join(msg));
                            }

                            if (args[0].equalsIgnoreCase("*")) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.chat(Joiner.on(" ").join(msg));
                                }
                            }
                        } else {
                            sender.sendMessage("§7Force a player to run a command or chat.");
                            sender.sendMessage("§7Usage: §b/sudo Dooder07 /restart");
                        }
                    }
                },1L);
                return false;
            }
        }
        return false;
    }
}