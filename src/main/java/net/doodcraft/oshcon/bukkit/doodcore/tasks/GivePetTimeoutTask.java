package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.listeners.PlayerListener;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GivePetTimeoutTask extends BukkitRunnable {

    private Player player;

    public GivePetTimeoutTask(Player p) {
        this.player = p;
        this.runTaskLater(DoodCorePlugin.plugin, 400L);
    }

    @Override
    public void run() {
        if (PlayerListener.waiting.containsKey(player.getUniqueId())) {
            if (player.isOnline()) {
                player.sendMessage("§cYour GivePet request timed out.");
            }
            PlayerListener.waiting.remove(player.getUniqueId());
            PlayerListener.requesting.remove(player.getUniqueId());
        }
    }
}