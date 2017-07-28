package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
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
                    sender.sendMessage("Invalid command.");
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

                    sender.sendMessage("Invalid args.");
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

                sender.sendMessage("Invalid command.");
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
            }
        }
        return false;
    }

    public void reload(CommandSender sender) {
        Settings.reload();
        sender.sendMessage("Plugin reloaded!");
    }
}
