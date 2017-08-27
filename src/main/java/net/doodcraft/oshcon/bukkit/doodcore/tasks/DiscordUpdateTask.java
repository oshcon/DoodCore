package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.scheduler.BukkitRunnable;

public class DiscordUpdateTask extends BukkitRunnable {
    @Override
    public void run() {
        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                try {
                    if (DiscordManager.client.isReady()) {
                        DiscordManager.updateTopic();
                        DiscordManager.updateGame();
                        return;
                    }
                    StaticMethods.log("Attempted to send data to Discord before the client was ready.");
                } catch (Exception ex) {
                    StaticMethods.log("There was an error sending data to Discord; relogging to attempt a fix.");
                    relog();
                }
            }
        }

        relog();
    }

    public static void relog() {
        if (!DiscordManager.client.isLoggedIn()) {
            DiscordManager.client.login();
            return;
        }
        DiscordManager.client.logout();
        DiscordManager.client.login();
    }
}