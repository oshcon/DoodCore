package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.UUID;

public class VotifierListener implements Listener {
    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid = PlayerMethods.getCrackedUUID(vote.getUsername());

        if (Bukkit.getPlayer(uuid) != null) {
            // They are online, message them and shit.
            CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);
            cPlayer.getPlayer().sendMessage("§aThank you for voting at " + vote.getServiceName() + ", " + cPlayer.getName() + "!");

            // check if first vote in 24 hour period
            if ((System.currentTimeMillis() - cPlayer.getLastVote()) >= 86400 * 1000L) { // greater than millis in 24 hours
                // its been more than 24 hours since the last vote, tell them theyve unlocked stuff
                Bukkit.broadcastMessage("§b" + vote.getUsername() + " just voted for the server! §8[§b/vote§8]");
                cPlayer.getPlayer().sendMessage("§7You've unlocked §b/home§7, §b/back§7, §b/tpa§7, and §b/tpahere");
            }

            cPlayer.setLastVote(System.currentTimeMillis());
            cPlayer.setThankedForOfflineVote(true);
            cPlayer.incrementTotalVotes();
        } else {
            // They are not online, modify data directly.
            Configuration data = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + (uuid + ".yml"));
            data.set("Voting.LastVote", System.currentTimeMillis());
            data.set("Voting.Thanked", false);
            data.set("Voting.Total", data.getInteger("Voting.Total") + 1);
            data.save();
        }
    }
}