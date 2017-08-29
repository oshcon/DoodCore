package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("pvp")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (cPlayer.getWarnedPVPExpiration()) {
                    player.sendMessage("§cYour PvP protection has already expired.");
                    return false;
                }

                if (cPlayer.getCurrentActiveTime() > (Settings.pvpProtection * 1000)) {
                    player.sendMessage("§cYour PvP protection has already expired.");
                    return false;
                }

                if (args.length == 0) {
                    player.sendMessage("§cThis command will §lirreversibly §cdisable your PvP protection,\n§cenabling you to deal and receive all combat damage.");
                    player.sendMessage("§cIf you are absolutely sure you want this,\n§ctoggle PvP on using §b/pvp enable");
                    return false;
                }

                if (args[0].equalsIgnoreCase("enable")) {
                    player.sendMessage("§6You manually disabled your PvP protection!");
                    player.sendMessage("§cYour two hour PvP protection has expired!");
                    cPlayer.setWarnedPVPExpiration(true);
                    return true;
                }

                sender.sendMessage("§cIncorrect argument.");
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}