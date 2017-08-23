package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordReminderTask implements Runnable {

    public static Map<UUID, Integer> tasks = new ConcurrentHashMap<>();

    CorePlayer cPlayer;

    public DiscordReminderTask(CorePlayer cPlayer) {
        this.cPlayer = cPlayer;
    }

    @Override
    public void run() {
        if (cPlayer != null) {
            if (!cPlayer.isIgnoringDiscordReminder()) {
                if (cPlayer.getPlayer() != null && cPlayer.getPlayer().isOnline()) {
                    if (cPlayer.getDiscordId() == 0L) {
                        Messages.sendMultiLine(cPlayer, "DiscordSyncReminder");
                    }
                }
            }
        }
    }
}