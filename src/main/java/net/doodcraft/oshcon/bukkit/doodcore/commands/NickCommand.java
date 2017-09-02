package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Map;

public class NickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("nick")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (!PlayerMethods.hasPermission(player, "core.command.nick", true)) {
                    return false;
                }

                if (args.length != 1) {
                    sender.sendMessage("§cPlease supply a valid nick name.");
                    if (sender.hasPermission("core.command.nick.colors")) {
                        sender.sendMessage("§7All color codes besides &k are valid.");
                    }
                    return false;
                }

                if (StaticMethods.removeColor(args[0]).toCharArray().length < 3) {
                    sender.sendMessage("§cPlease supply a valid nick name longer than 2 characters.");
                    return false;
                }

                if (StaticMethods.removeColor(args[0]).toCharArray().length > 16) {
                    sender.sendMessage("§cPlease supply a valid nick name shorter than 17 characters.");
                    return false;
                }

                if (!args[0].equalsIgnoreCase(cPlayer.getName()) && !args[0].equalsIgnoreCase(cPlayer.getNick())) {
                    Iterator it = CorePlayer.names.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        if (args[0].equalsIgnoreCase(String.valueOf(pair.getKey()))) {
                            sender.sendMessage("§cYou cannot use that nickname.");
                            it.remove();
                            return false;
                        }
                        if (args[0].equalsIgnoreCase(StaticMethods.removeColor(String.valueOf(pair.getValue())))) {
                            sender.sendMessage("§cYou cannot use that nickname.");
                            it.remove();
                            return false;
                        }
                        it.remove();
                    }
                }

                if (PlayerMethods.hasPermission(player, "core.command.nick.colors", false)) {
                    cPlayer.setNick(StaticMethods.addColor(args[0]).replaceAll("§k", ""));
                } else {
                    cPlayer.setNick(StaticMethods.removeColor(args[0]));
                }

                sender.sendMessage("§aNick set successfully, " + cPlayer.getNick() + "§a!");
                CorePlayer.names.put(cPlayer.getName(), cPlayer.getNick());
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}