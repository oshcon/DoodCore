package net.doodcraft.oshcon.bukkit.doodcore.commands;

import mkremins.fanciful.FancyMessage;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.Lag;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("core")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.core", true)) {
                    return false;
                }

                if (args.length == 0) {
                    sender.sendMessage("Valid commands: reload, purge, togglediscord, tps, uuid, listkills");
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!PlayerMethods.hasPermission(player, "core.command.reload", true)) {
                        return false;
                    }

                    reload(sender);
                    return true;
                }

                if (args[0].equalsIgnoreCase("purge")) {
                    if (!PlayerMethods.hasPermission(player, "core.command.purge", true)) {
                        return false;
                    }

                    if (args.length != 2) {
                        sender.sendMessage("Not enough args.");
                        return false;
                    }

                    if (args[1].toLowerCase().startsWith("i")) {
                        for (World world : Bukkit.getWorlds()) {
                            sender.sendMessage("§8[§r" + world.getName() + "§8] §7Removed §b" + StaticMethods.purgeItemDrops(world) + " §7items.");
                        }
                        return true;
                    }

                    if (args[1].toLowerCase().startsWith("m")) {
                        for (World world : Bukkit.getWorlds()) {
                            sender.sendMessage("§8[§r" + world.getName() + "§8] §7Removed §b" + StaticMethods.purgeMonsters(world) + " §7monsters.");
                        }
                        return true;
                    }

                    sender.sendMessage("Valid args: items, monsters");
                    return false;
                }

                if (args[0].equalsIgnoreCase("togglediscord")) {
                    if (!PlayerMethods.hasPermission(player, "core.command.togglediscord", true)) {
                        return false;
                    }

                    if (DiscordManager.toggled) {
                        DiscordManager.toggled = false;
                        sender.sendMessage("Discord <-> Minecraft communication has been disabled.");
                        return true;
                    } else {
                        DiscordManager.toggled = true;
                        sender.sendMessage("Discord <-> Minecraft communication has been enabled.");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("tps")) {
                    if (!PlayerMethods.hasPermission(player, "core.command.tps", true)) {
                        return false;
                    }

                    sender.sendMessage("Current Tick Rate: " + Lag.getTPS());
                    return true;
                }

                if (args[0].equalsIgnoreCase("uuid")) {
                    if (!PlayerMethods.hasPermission(player, "core.command.uuid", true)) {
                        return false;
                    }

                    if (args.length != 2) {
                        sender.sendMessage("Please supply a name.");
                        return false;
                    }

                    FancyMessage msg = new FancyMessage(PlayerMethods.getCrackedUUID(args[1]).toString());
                    msg.suggest(PlayerMethods.getCrackedUUID(args[1]).toString());
                    msg.send(player);
                    return true;
                }

                if (args[0].equalsIgnoreCase("listkills")) {
                    if (args.length > 1) {
                        if (CorePlayer.getPlayer(args[2]) != null) {
                            CorePlayer killer = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[2]).getUniqueId());
                            if (killer.getKills().size() > 0) {
                                sender.sendMessage(killer.getName() + "'s kills:");
                                for (String kill : killer.getKills().keySet()) {
                                    sender.sendMessage(kill + ": " + killer.getKills().get(kill));
                                }
                                return true;
                            } else {
                                sender.sendMessage("They have no kills.");
                                return false;
                            }
                        } else {
                            sender.sendMessage("Player is not online.");
                            return false;
                        }
                    }

                    sender.sendMessage("Invalid args.");
                    return false;
                }

                sender.sendMessage("Valid commands: reload, purge, togglediscord, tps, uuid, listkills");
                return false;
            } else {
                if (args.length == 0) {
                    sender.sendMessage("Invalid command.");
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    reload(sender);
                    return true;
                }

                if (args[0].equalsIgnoreCase("purge")) {
                    if (args.length != 2) {
                        sender.sendMessage("Not enough args.");
                        return false;
                    }
                    if (args[1].toLowerCase().startsWith("i")) {
                        for (World world : Bukkit.getWorlds()) {
                            sender.sendMessage("§8[§r" + world.getName() + "§8] §7Removed §b" + StaticMethods.purgeItemDrops(world) + " §7items.");
                        }
                        return true;
                    }
                    if (args[1].toLowerCase().startsWith("m")) {
                        for (World world : Bukkit.getWorlds()) {
                            sender.sendMessage("§8[§r" + world.getName() + "§8] §7Removed §b" + StaticMethods.purgeMonsters(world) + " §7monsters.");
                        }
                        return true;
                    }

                    sender.sendMessage("Invalid args.");
                    return false;
                }

                if (args[0].equalsIgnoreCase("togglediscord")) {
                    if (DiscordManager.toggled) {
                        DiscordManager.toggled = false;
                        sender.sendMessage("Discord <-> Minecraft communication has been disabled.");
                    } else {
                        DiscordManager.toggled = true;
                        sender.sendMessage("Discord <-> Minecraft communication has been enabled.");
                    }
                }

                if (args[0].equalsIgnoreCase("tps")) {
                    sender.sendMessage("Current Tick Rate: " + Lag.getTPS());
                    return true;
                }

                if (args[0].equalsIgnoreCase("uuid")) {

                    if (args.length != 2) {
                        sender.sendMessage("Please supply a name.");
                        return false;
                    }

                    sender.sendMessage(PlayerMethods.getCrackedUUID(args[1]).toString());
                    return true;
                }

                if (args[0].equalsIgnoreCase("listkills")) {
                    if (args.length > 1) {
                        if (CorePlayer.getPlayer(args[2]) != null) {
                            CorePlayer killer = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[2]).getUniqueId());
                            if (killer.getKills().size() > 0) {
                                sender.sendMessage(killer.getName() + "'s kills:");
                                for (String kill : killer.getKills().keySet()) {
                                    sender.sendMessage(kill + ": " + killer.getKills().get(kill));
                                }
                                return true;
                            } else {
                                sender.sendMessage("They have no kills.");
                                return false;
                            }
                        } else {
                            sender.sendMessage("Player is not online.");
                            return false;
                        }
                    }

                    sender.sendMessage("Invalid args.");
                    return false;
                }
            }
        }
        return false;
    }

    public void reload(CommandSender sender) {
        Settings.reload();
        sender.sendMessage("Plugin reloaded!");
    }
}