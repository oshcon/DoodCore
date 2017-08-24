package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class SeenCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("seen")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.seen", true)) {
                    return false;
                }

                if (args.length <= 0) {
                    sender.sendMessage("Please input a username to lookup.");
                    return false;
                }

                if (new File(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + PlayerMethods.getCrackedUUID(args[0]) + ".yml").exists()) {
                    Configuration cData = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + PlayerMethods.getCrackedUUID(args[0]) + ".yml");
                    if (Bukkit.getPlayer(UUID.fromString(cData.getString("UUID"))) != null) {
                        player.sendMessage("§7Status: §aONLINE\n§7LastJoined: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastJoined"))) + "\n§7LastQuit: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastQuit"))));
                        return true;
                    } else {
                        player.sendMessage("§7Status: §cOFFLINE\n§7LastJoined: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastJoined"))) + "\n§7LastQuit: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastQuit"))));
                        return true;
                    }
                }

                sender.sendMessage("That user could not be found. *Name lookups are case sensitive.*");
                return false;
            } else {
                if (args.length <= 0) {
                    sender.sendMessage("Please input a username to lookup.");
                    return false;
                }

                if (new File(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + PlayerMethods.getCrackedUUID(args[0]) + ".yml").exists()) {
                    Configuration cData = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + PlayerMethods.getCrackedUUID(args[0]) + ".yml");
                    if (Bukkit.getPlayer(UUID.fromString(cData.getString("UUID"))) != null) {
                        StaticMethods.log("§7Status: §aONLINE\n§7LastJoined: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastJoined"))) + "\n§7LastQuit: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastQuit"))));
                        return true;
                    } else {
                        StaticMethods.log("§7Status: §cOFFLINE\n§7LastJoined: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastJoined"))) + "\n§7LastQuit: §b" + StaticMethods.getTimeStamp(Long.valueOf(cData.getString("LastQuit"))));
                        return true;
                    }
                }

                sender.sendMessage("That user could not be found. *Name lookups are case sensitive.*");
                return false;
            }
        }
        return false;
    }
}