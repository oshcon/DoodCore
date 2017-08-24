package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.TpaTimeoutTask;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class TpacceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tpaccept")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.tpaccept", true)) {
                    return false;
                }

                UUID uuid = player.getUniqueId();
                ArrayList<UUID> requestingTpa = new ArrayList<>();
                ArrayList<UUID> requestingTpahere = new ArrayList<>();

                for (UUID key : TpaCommand.requesting.keySet()) {
                    // Found a player who sent a request to us
                    if (TpaCommand.requesting.get(key).equals(uuid)) {
                        requestingTpa.add(key);
                    }
                }

                for (UUID key : TpahereCommand.requesting.keySet()) {
                    // Found a player who sent a request to us
                    if (TpahereCommand.requesting.get(key).equals(uuid)) {
                        requestingTpahere.add(key);
                    }
                }

                if (requestingTpa.size() + requestingTpahere.size() <= 0) {
                    // There are no requests to us.
                    player.sendMessage("§cYou have no pending requests. Maybe they timed out?");
                    return true;
                }

                if (requestingTpa.size() + requestingTpahere.size() > 1) {
                    if (args.length == 1) {
                        String name = args[0];
                        for (UUID u : requestingTpa) {
                            if (Bukkit.getPlayer(name) != null) {
                                if (Bukkit.getPlayer(name).getUniqueId().equals(u)) {
                                    Player requester = Bukkit.getPlayer(u);
                                    requester.sendMessage("§7" + player.getName() + " accepted your request. Preparing to teleport you...");
                                    new WarmupTeleportTask(requester, player.getLocation(), player, "§7Teleported you to " + player.getName() + ".", "tpaccept", 5000);
                                    TpaCommand.requesting.remove(u);
                                    return true;
                                }
                            }
                        }
                        for (UUID u : requestingTpahere) {
                            if (Bukkit.getPlayer(name) != null) {
                                if (Bukkit.getPlayer(name).getUniqueId().equals(u)) {
                                    Player requester = Bukkit.getPlayer(u);
                                    requester.sendMessage("§7" + player.getName() + " accepted your request. Preparing to teleport them...");
                                    new WarmupTeleportTask(player, requester.getLocation(), requester, "§7Teleported you to " + requester.getName() + ".", "tpaccept", 5000);
                                    TpahereCommand.requesting.remove(u);
                                    return true;
                                }
                            }
                        }
                        return true;
                    }

                    player.sendMessage("§7You have multiple pending teleport requests.");
                    player.sendMessage("§7You must choose which player to accept.");

                    ArrayList<String> tpas = new ArrayList<>();
                    ArrayList<String> tpaheres = new ArrayList<>();

                    if (requestingTpa.size() > 0) {
                        for (UUID u : requestingTpa) {
                            if (Bukkit.getPlayer(u) != null) {
                                tpas.add(Bukkit.getPlayer(u).getName());
                            }
                        }
                    }

                    if (requestingTpahere.size() > 0) {
                        for (UUID u : requestingTpa) {
                            if (Bukkit.getPlayer(u) != null) {
                                tpaheres.add(Bukkit.getPlayer(u).getName());
                            }
                        }
                    }

                    if (tpas.size() > 0) {
                        player.sendMessage("§7Tpa: §b" + Joiner.on("§7, §b").join(tpas));
                    }

                    if (tpaheres.size() > 0) {
                        player.sendMessage("§7Tpahere: §b" + Joiner.on("§7, §b").join(tpaheres));
                    }

                    return true;
                }

                // There was only one request. Accept it then remove it.
                if (requestingTpa.size() == 1) {
                    // Teleport the requester to the player

                    // check online status first.
                    if (Bukkit.getPlayer(requestingTpa.get(0)) == null) {
                        // They are not online. Remove the request
                        player.sendMessage("§cThat player is no longer online.");
                        new TpaTimeoutTask(requestingTpa.get(0), 1L);
                    }

                    Player requester = Bukkit.getPlayer(requestingTpa.get(0));
                    requester.sendMessage("§7" + player.getName() + " accepted your request. Preparing to teleport you...");
                    new WarmupTeleportTask(requester, player.getLocation(), player, "§7Teleported you to " + player.getName() + ".", "tpaccept", 5000);
                    TpaCommand.requesting.remove(requestingTpa.get(0));
                    return true;
                }

                if (requestingTpahere.size() == 1) {
                    // Teleport the player to the requester

                    // check online status first.
                    if (Bukkit.getPlayer(requestingTpahere.get(0)) == null) {
                        // They are not online. Remove the request
                        player.sendMessage("§cThat player is no longer online.");
                        new TpaTimeoutTask(requestingTpahere.get(0), 1L);
                    }

                    Player requester = Bukkit.getPlayer(requestingTpahere.get(0));
                    requester.sendMessage("§7" + player.getName() + " accepted your request. Preparing to teleport them...");
                    new WarmupTeleportTask(player, requester.getLocation(), requester, "§7Teleported you to " + requester.getName() + ".", "tpaccept", 5000);
                    TpahereCommand.requesting.remove(requestingTpahere.get(0));
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