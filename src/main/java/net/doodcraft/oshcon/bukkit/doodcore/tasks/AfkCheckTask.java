package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AfkCheckTask implements Runnable {

    Player player;
    UUID uuid;

    public AfkCheckTask(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);

        if (cPlayer == null) {
            StaticMethods.log("Null cPlayer for AFK task: " + this.player.getName());
            return;
        }

        AfkHandler.lastLocation.computeIfAbsent(uuid, k -> AfkHandler.locString(this.player.getLocation()));
        AfkHandler.lastAction.computeIfAbsent(uuid, k -> System.currentTimeMillis());

        if (AfkHandler.lastLocation.get(uuid).equals(AfkHandler.locString(this.player.getLocation()))) {
            if ((System.currentTimeMillis() - AfkHandler.lastAction.get(uuid)) >= (long) (360 * 1000)) {
                if (!cPlayer.isCurrentlyAfk()) {
                    cPlayer.setAfkStatus(true, "Idling§r");
                    return;
                }
                if ((System.currentTimeMillis() - AfkHandler.lastAction.get(uuid)) >= (long) (900 * 1000)) {
                    if (!cPlayer.getPlayer().hasPermission("core.bypass.afkkick")) {
                        cPlayer.getPlayer().kickPlayer("§bYou were kicked for AFK.");
                    }
                }
            }
        } else {
            if (cPlayer.isCurrentlyAfk()) {
                if (!cPlayer.isVanished()) {
                    cPlayer.setAfkStatus(false, "Who cares about the reason?§r");
                }
            }
            AfkHandler.lastAction.put(uuid, System.currentTimeMillis());
        }
        AfkHandler.lastLocation.put(uuid, AfkHandler.locString(this.player.getLocation()));
    }
}