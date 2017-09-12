package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

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

                if (args.length >= 1) {
                    if (player.hasPermission("core.command.homes.others")) {
                        if (Bukkit.getPlayer(args[0]) != null) {
                            // They are online.
                            CorePlayer c = CorePlayer.getPlayers().get(Bukkit.getPlayer(args[0]).getUniqueId());
                            if (cPlayer.getHomes().size() >= 1) {
                                player.sendMessage(c.getColorPrefix() + c.getNick() + "§7's Homes: ");
                                for (String name : cPlayer.getHomes().keySet()) {
                                    player.sendMessage("§8:: §b" + name + "§7: " + cPlayer.getHomes().get(name));
                                }
                                return true;
                            } else {
                                player.sendMessage("§cThey have no homes set.");
                                return false;
                            }
                        } else {
                            File directory = new File(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data");
                            File[] files = directory.listFiles();

                            if (files != null) {
                                for (File f : files) {
                                    if (f.getName().equalsIgnoreCase(PlayerMethods.getCrackedUUID(args[0]).toString() + ".yml")) {
                                        Configuration cData = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + f.getName());
                                        if (cData.getYaml().getConfigurationSection("Homes").getKeys(false).size() >= 1) {
                                            player.sendMessage(cData.getString("Name") + "'s Homes:");
                                            for (String id : cData.getYaml().getConfigurationSection("Homes").getKeys(false)) {
                                                player.sendMessage("§8:: §b" + id + "§7: " + cData.getString("Homes." + id));
                                            }
                                            return true;
                                        } else {
                                            // no homes
                                            player.sendMessage("§cThey have no homes.");
                                            return false;
                                        }
                                    }
                                }
                            }

                            player.sendMessage("§cCannot find the specified player. Offline checking is CaSe SeNsItIvE.");
                            return false;
                        }
                    }
                }

                if (cPlayer.getHomes().size() >= 1) {
                    cPlayer.getPlayer().sendMessage("§7Your homes: ");
                    cPlayer.getPlayer().sendMessage("§b" + Joiner.on("§7, §b").join(cPlayer.getHomes().keySet()));
                    return true;
                } else {
                    cPlayer.getPlayer().sendMessage("§cYou do not have any homes.");
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