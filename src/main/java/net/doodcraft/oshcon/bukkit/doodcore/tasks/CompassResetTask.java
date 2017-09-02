package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.commands.TrackCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassResetTask extends BukkitRunnable {

    Player player;

    public CompassResetTask(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (player != null) {
            player.sendMessage("§cYour compass reset.");
            TrackCommand.resetCompass(this.player);
        }
    }
}
