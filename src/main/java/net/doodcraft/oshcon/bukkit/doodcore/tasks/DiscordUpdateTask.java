package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;

public class DiscordUpdateTask implements Runnable {
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