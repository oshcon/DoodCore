package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
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
                            cPlayer.getPlayer().sendMessage("§cYour two hour PvP protection has expired!");
                            cPlayer.setWarnedPVPExpiration(true);
                        }
                    }
                }

                // check if veteran
                if (l > (Settings.veteranTime * 1000)) {
                    DiscordManager.autoRankVeteran(cPlayer);
                }

                // payout
                payout(cPlayer);
            }

            return;
        }

        this.cancel();
    }

    public static void payout(CorePlayer cPlayer) {
        // get last payout time, if it's 30 minutes or greater since their last payout, perform a new payout
        if (cPlayer.timeToNextPayout() <= 0L) {

            // payout $50/30m or (1 2/3)/m

            double add = StaticMethods.round(((double) ((cPlayer.getCurrentActiveTime() - cPlayer.getLastPayout())/60000L)) * 1.66666666666667, 2);

            Vault.economy.depositPlayer(cPlayer.getPlayer(), add);

            cPlayer.getPlayer().sendMessage("§aYou earned §6§l$" + add + "§a, just for playing! §8[§b/bal§8] [§b/mytime§8]");

            cPlayer.setLastPayout(cPlayer.getCurrentActiveTime());
        }
    }
}