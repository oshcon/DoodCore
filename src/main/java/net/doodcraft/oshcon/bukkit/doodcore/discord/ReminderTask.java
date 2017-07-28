package net.doodcraft.oshcon.bukkit.doodcore.discord;

import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReminderTask implements Runnable {

    public static Map<UUID, Integer> tasks = new ConcurrentHashMap<>();

    CorePlayer cPlayer;

    public ReminderTask(CorePlayer cPlayer) {
        this.cPlayer = cPlayer;
    }

    @Override
    public void run() {
        if (cPlayer != null) {
            if (!cPlayer.isIgnoringDiscordReminder()) {
                if (cPlayer.getPlayer().isOnline()) {
                    if (cPlayer.getDiscordUserId() == 0) {
                        Messages.sendMultiLine(cPlayer, "DiscordSyncReminder");
                    }
                }
            }
        }
    }
}