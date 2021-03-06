package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class MyTimeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("mytime")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (args.length >= 1) {
                    if (player.hasPermission("core.command.mytime.others")) {

                        // Check for online player first.
                        if (CorePlayer.getPlayer(args[0]) != null) {
                            CorePlayer cPlayer = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[0]).getUniqueId());

                            player.sendMessage("§7" + cPlayer.getName() + "'s Time: ");
                            player.sendMessage("§8:: §7Online Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentActiveTime() + cPlayer.getCurrentAfkTime()));
                            player.sendMessage("§8:: §7Active Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentActiveTime()));
                            player.sendMessage("§8:: §7AFK Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentAfkTime()));
                            try {
                                player.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(cPlayer.timeToNextPayout()));
                            } catch (Exception ex) {
                                player.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(1L));
                            }
                            if (cPlayer.getCurrentActiveTime() < (Settings.veteranTime * 1000) && !cPlayer.isVeteran()) {
                                player.sendMessage("§8:: §7Veteran Rankup: §3" + StaticMethods.getDurationBreakdown((Settings.veteranTime * 1000) - cPlayer.getCurrentActiveTime()));
                            } else {
                                player.sendMessage("§8:: §7Veteran Rankup: §8[§aACQUIRED§8]");
                            }
                            if (cPlayer.getCurrentActiveTime() < (Settings.pvpProtection * 1000) && !cPlayer.getWarnedPVPExpiration()) {
                                player.sendMessage("§8:: §7PvP Protection: §3" + StaticMethods.getDurationBreakdown((Settings.pvpProtection * 1000) - cPlayer.getCurrentActiveTime()));
                            } else {
                                player.sendMessage("§8:: §7PvP Protection: §8[§cEXPIRED§8]");
                            }
                            return true;
                        }

                        // Not online, lets check for stored player data
                        File directory = new File(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data");
                        File[] files = directory.listFiles();

                        if (files != null) {
                            for (File f : files) {
                                if (f.getName().equalsIgnoreCase(PlayerMethods.getCrackedUUID(args[0]).toString() + ".yml")) {
                                    Configuration cData = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + f.getName());
                                    if (Bukkit.getPlayer(UUID.fromString(cData.getString("UUID"))) != null) {
                                        player.sendMessage("§7" + args[0] + "'s Time: ");
                                        player.sendMessage("§8:: §7Online Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.ActiveTime")) + Long.valueOf(cData.getString("AfkTime"))));
                                        player.sendMessage("§8:: §7Active Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.ActiveTime"))));
                                        player.sendMessage("§8:: §7AFK Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.AfkTime"))));
                                        return true;
                                    }
                                }
                            }
                        }

                        player.sendMessage("§cCannot find the specified player. Offline checking is CaSe SeNsItIvE.");
                        return false;
                    }
                }

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
                if (cPlayer != null) {
                    Long activeTime = cPlayer.getCurrentActiveTime();
                    Long afkTime = cPlayer.getCurrentAfkTime();

                    player.sendMessage("§7" + player.getName() + "'s Time: ");
                    player.sendMessage("§8:: §7Online Time: §3" + StaticMethods.getDurationBreakdown(activeTime + afkTime));
                    player.sendMessage("§8:: §7Active Time: §3" + StaticMethods.getDurationBreakdown(activeTime));
                    player.sendMessage("§8:: §7AFK Time: §3" + StaticMethods.getDurationBreakdown(afkTime));
                    try {
                        player.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(cPlayer.timeToNextPayout()));
                    } catch (Exception ex) {
                        player.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(1L));
                    }
                    if (cPlayer.getCurrentActiveTime() < (Settings.veteranTime * 1000) && !cPlayer.isVeteran()) {
                        player.sendMessage("§8:: §7Veteran Rankup: §3" + StaticMethods.getDurationBreakdown((Settings.veteranTime * 1000) - cPlayer.getCurrentActiveTime()));
                    } else {
                        player.sendMessage("§8:: §7Veteran Rankup: §8[§aACQUIRED§8]");
                    }
                    if (cPlayer.getCurrentActiveTime() < (Settings.pvpProtection * 1000) && !cPlayer.getWarnedPVPExpiration()) {
                        player.sendMessage("§8:: §7PvP Protection: §3" + StaticMethods.getDurationBreakdown((Settings.pvpProtection * 1000) - cPlayer.getCurrentActiveTime()));
                    } else {
                        player.sendMessage("§8:: §7PvP Protection: §8[§cEXPIRED§8]");
                    }
                }
                return true;
            } else {
                if (args.length >= 1) {
                    // Check for online player first.
                    if (CorePlayer.getPlayer(args[0]) != null) {
                        CorePlayer cPlayer = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[0]).getUniqueId());

                        sender.sendMessage("§7" + cPlayer.getName() + "'s Time: ");
                        sender.sendMessage("§8:: §7Online Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentActiveTime() + cPlayer.getCurrentAfkTime()));
                        sender.sendMessage("§8:: §7Active Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentActiveTime()));
                        sender.sendMessage("§8:: §7AFK Time: §3" + StaticMethods.getDurationBreakdown(cPlayer.getCurrentAfkTime()));
                        try {
                            sender.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(cPlayer.timeToNextPayout()));
                        } catch (Exception ex) {
                            sender.sendMessage("§8:: §7Next Reward: §3" + StaticMethods.getDurationBreakdown(1L));
                        }
                        if (cPlayer.getCurrentActiveTime() < (Settings.veteranTime * 1000) && !cPlayer.isVeteran()) {
                            sender.sendMessage("§8:: §7Veteran Rankup: §3" + StaticMethods.getDurationBreakdown((Settings.veteranTime * 1000) - cPlayer.getCurrentActiveTime()));
                        } else {
                            sender.sendMessage("§8:: §7Veteran Rankup: §8[§aACQUIRED§8]");
                        }
                        if (cPlayer.getCurrentActiveTime() < (Settings.pvpProtection * 1000) && !cPlayer.getWarnedPVPExpiration()) {
                            sender.sendMessage("§8:: §7PvP Protection: §3" + StaticMethods.getDurationBreakdown((Settings.pvpProtection * 1000) - cPlayer.getCurrentActiveTime()));
                        } else {
                            sender.sendMessage("§8:: §7PvP Protection: §8[§cEXPIRED§8]");
                        }
                        return true;
                    }

                    // Not online, lets check for stored player data
                    File directory = new File(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data");
                    File[] files = directory.listFiles();

                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().equalsIgnoreCase(PlayerMethods.getCrackedUUID(args[0]).toString() + ".yml")) {
                                Configuration cData = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + f.getName());
                                if (Bukkit.getPlayer(UUID.fromString(cData.getString("UUID"))) != null) {
                                    sender.sendMessage("§7" + args[0] + "'s Time: ");
                                    sender.sendMessage("§8:: §7Online Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.ActiveTime")) + Long.valueOf(cData.getString("AfkTime"))));
                                    sender.sendMessage("§8:: §7Active Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.ActiveTime"))));
                                    sender.sendMessage("§8:: §7AFK Time: §3" + StaticMethods.getDurationBreakdown(Long.valueOf(cData.getString("Time.AfkTime"))));
                                    return true;
                                }
                            }
                        }
                    }

                    sender.sendMessage("§cCannot find the specified player. Offline checking is CaSe SeNsItIvE.");
                    return false;
                }

                sender.sendMessage("Please supply a player name.");
                return false;
            }
        }
        return false;
    }
}