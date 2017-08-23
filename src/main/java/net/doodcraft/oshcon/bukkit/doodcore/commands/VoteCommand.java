package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("vote")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
                player.sendMessage("§7Your Votes: §b" + cPlayer.getTotalVotes());
                if (cPlayer.getLastVote() > 0L) {
                    player.sendMessage("§7Last Vote: §b" + StaticMethods.getTimeStamp(cPlayer.getLastVote()));
                } else {
                    player.sendMessage("§7Last Vote: §cNever");
                }
                if (PlayerMethods.hasVotedToday(player)) {
                    player.sendMessage("§7Voted in the last 24 hours? §8[§aYES§8]");
                } else {
                    player.sendMessage("§7Voted in the last 24 hours? §8[§cNO§8]");
                }
                player.sendMessage("§7Voting unlocks §b/home§7, §b/back§7, §b/tpa§7, and §b/tpahere");
                player.sendMessage("§7You can vote at any of the following sites:");
                Messages.sendMultiLine(cPlayer, "VoteSites");
                return true;
            } else {
                sender.sendMessage("Console cannot vote.");
                return false;
            }
        }
        return false;
    }
}