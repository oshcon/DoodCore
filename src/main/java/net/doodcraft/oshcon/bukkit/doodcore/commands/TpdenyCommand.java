package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.TpaTimeoutTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class TpdenyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tpdeny")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.tpdeny", true)) {
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
                            if (CorePlayer.getPlayer(name) != null) {
                                if (CorePlayer.getPlayer(name).getUniqueId().equals(u)) {
                                    Player requester = Bukkit.getPlayer(u);
                                    player.sendMessage("§7You denied " + requester.getName() + "'s tpa request.");
                                    requester.sendMessage("§7" + player.getName() + " denied your tpa request.");
                                    TpaCommand.requesting.remove(u);
                                    return true;
                                }
                            }
                        }
                        for (UUID u : requestingTpahere) {
                            if (CorePlayer.getPlayer(name) != null) {
                                if (CorePlayer.getPlayer(name).getUniqueId().equals(u)) {
                                    Player requester = Bukkit.getPlayer(u);
                                    player.sendMessage("§7You denied " + requester.getName() + "'s tpahere request.");
                                    requester.sendMessage("§7" + player.getName() + " denied your tpahere request.");
                                    TpahereCommand.requesting.remove(u);
                                    return true;
                                }
                            }
                        }
                        return true;
                    }

                    player.sendMessage("§7You have multiple pending teleport requests.");
                    player.sendMessage("§7You must choose which player to deny.");

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

                // There was only one request. Deny it then remove it.
                if (requestingTpa.size() == 1) {
                    // check online status first.
                    if (Bukkit.getPlayer(requestingTpa.get(0)) == null) {
                        // They are not online. Remove the request
                        player.sendMessage("§cThat player is no longer online.");
                        new TpaTimeoutTask(requestingTpa.get(0), 1L);
                    }

                    Player requester = Bukkit.getPlayer(requestingTpa.get(0));
                    player.sendMessage("§7You denied " + requester.getName() + "'s tpa request.");
                    requester.sendMessage("§7" + player.getName() + " denied your tpa request.");
                    TpaCommand.requesting.remove(requestingTpa.get(0));
                    return true;
                }

                if (requestingTpahere.size() == 1) {
                    // check online status first.
                    if (Bukkit.getPlayer(requestingTpahere.get(0)) == null) {
                        // They are not online. Remove the request
                        player.sendMessage("§cThat player is no longer online.");
                        new TpaTimeoutTask(requestingTpahere.get(0), 1L);
                    }

                    Player requester = Bukkit.getPlayer(requestingTpahere.get(0));
                    player.sendMessage("§7You denied " + requester.getName() + "'s tpahere request.");
                    requester.sendMessage("§7" + player.getName() + " denied your tpahere request.");
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