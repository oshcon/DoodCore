package net.doodcraft.oshcon.bukkit.doodcore.discord;

public class DiscordTask implements Runnable {
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