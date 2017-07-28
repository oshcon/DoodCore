package net.doodcraft.oshcon.bukkit.doodcore.afk;

import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AfkTask implements Runnable {

    Player player;
    UUID uuid;

    public AfkTask(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        run();
    }

    @Override
    public void run() {
        CorePlayer cPlayer = CorePlayer.players.get(uuid);

        if (cPlayer == null) {
            StaticMethods.log("Null cPlayer for AFK task: " + this.player.getName());
            return;
        }

        AfkHandler.lastLocation.computeIfAbsent(uuid, k -> AfkHandler.locString(this.player.getLocation()));
        AfkHandler.lastAction.computeIfAbsent(uuid, k -> System.currentTimeMillis());

        if (AfkHandler.lastLocation.get(uuid).equals(AfkHandler.locString(this.player.getLocation()))) {
            if ((System.currentTimeMillis() - AfkHandler.lastAction.get(uuid)) >= (long) (300 * 1000)) {
                if (!cPlayer.isCurrentlyAfk()) {
                    cPlayer.setAfkStatus(true, "Idling§r");
                    return;
                }
                if ((System.currentTimeMillis() - AfkHandler.lastAction.get(uuid)) >= (long) (600 * 1000)) {
                    if (!cPlayer.getPlayer().hasPermission("core.afk.kickbypass")) {
                        cPlayer.getPlayer().kickPlayer("§bYou were kicked for being AFK too long.");
                    }
                }
            }
        } else {
            if (cPlayer.isCurrentlyAfk()) {
                cPlayer.setAfkStatus(false, "Who cares about the reason?§r");
            }
            AfkHandler.lastAction.put(uuid, System.currentTimeMillis());
        }
        AfkHandler.lastLocation.put(uuid, AfkHandler.locString(this.player.getLocation()));
    }
}