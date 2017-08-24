package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import org.bukkit.scheduler.BukkitRunnable;

public class DiscordUpdateTask extends BukkitRunnable {
    @Override
    public void run() {
        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                DiscordManager.updateTopic();
                DiscordManager.updateGame();
            }
        }
    }
}