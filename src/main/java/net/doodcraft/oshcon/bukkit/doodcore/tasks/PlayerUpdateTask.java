package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerUpdateTask extends BukkitRunnable {

    UUID uuid;

    public PlayerUpdateTask(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        if (Bukkit.getPlayer(this.uuid) != null) {
            if (CorePlayer.getPlayers().containsKey(this.uuid)) {
                CorePlayer cPlayer = CorePlayer.getPlayers().get(this.uuid);
                Long l = cPlayer.getCurrentActiveTime();

                // check pvp expiration
                if (l > (Settings.pvpProtection * 1000)) {
                    if (cPlayer.getPlayer() != null) {
                        // Warn them.
                        if (!cPlayer.getWarnedPVPExpiration()) {
                            cPlayer.getPlayer().sendMessage("§cYour six hour PvP protection has expired!");
                            cPlayer.setWarnedPVPExpiration(true);
                        }
                    }
                }

                // check if veteran
                if (l > (Settings.veteranTime * 1000)) {
                    DiscordManager.autoRankVeteran(cPlayer);
                }
            }

            return;
        }

        this.cancel();
    }
}