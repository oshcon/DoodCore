package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.NumberConverter;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VotifierListener implements Listener {

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid;

        uuid = PlayerMethods.getCrackedUUID(vote.getUsername());

        StaticMethods.log("Received vote for " + vote.getUsername() + " from " + vote.getServiceName() + ":" + vote.getAddress());

        // Check each online player's name for case insensitivity.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().equals(vote.getUsername().toLowerCase())) {
                uuid = PlayerMethods.getCrackedUUID(p.getName());
            }
        }

        if (Bukkit.getPlayer(uuid) != null) {
            // They are online, message them.
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
            // If the UUID/name needs to be added, it's likely the player made a typo in their name.
            // This can be solved more easily when I write my own vote listener in node.js
            data.add("UUID", uuid);
            data.add("UUID", vote.getUsername());
            data.set("Voting.LastVote", System.currentTimeMillis());
            data.set("Voting.Thanked", false);
            data.set("Voting.Total", data.getInteger("Voting.Total") + 1);
            data.save();
        }
    }

    public static void giveVoteFlares(CorePlayer cPlayer) {
        int votes = cPlayer.getTotalVotes();
        int given = cPlayer.getTotalFlaresGiven();
        int owed = votes - given;

        if (owed >= 1) {
            // They are owed vote flares
            ItemStack flare = new ItemStack(Material.REDSTONE_TORCH_OFF, owed);
            ItemMeta flareMeta = flare.getItemMeta();
            flareMeta.setDisplayName("§c§lVote Flare");
            List<String> lore = new ArrayList<>();
            lore.add("Place me on the ground to win a random reward!");
            flareMeta.setLore(lore);
            flare.setItemMeta(flareMeta);
            if (owed > 1) {
                cPlayer.getPlayer().sendMessage("§aYou earned " + NumberConverter.convert(owed) + " vote flares.");
            } else {
                cPlayer.getPlayer().sendMessage("§You earned one vote flare.");
            }
            cPlayer.getPlayer().getInventory().addItem(flare);
            cPlayer.setTotalFlaresGiven(given + owed);
        }
    }
}