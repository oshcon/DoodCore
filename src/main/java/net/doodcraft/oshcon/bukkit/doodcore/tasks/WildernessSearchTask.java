package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class WildernessSearchTask extends BukkitRunnable {

    Player player;
    Location start;
    World world;
    int count;

    public WildernessSearchTask(Player player, int count) {
        this.player = player;
        this.start = player.getLocation();
        this.world = Bukkit.getWorlds().get(0);
        this.count = count;
    }

    @Override
    public void run() {
        if (!this.player.isOnline() || this.player.isDead()) {
            StaticMethods.log("Wilderness teleport for " + this.player.getName() + " was cancelled.");
            this.cancel();
            return;
        }

        if (this.player.getLocation().distanceSquared(start) >= 2) {
            StaticMethods.log("Wilderness teleport for " + this.player.getName() + " was cancelled.");
            this.cancel();
            return;
        }

        int radius = Settings.wildRadius;

        Random rand = DoodCorePlugin.random;
        int x = (rand.nextInt(radius * 2) - radius) + (int) this.world.getSpawnLocation().getX();
        int z = (rand.nextInt(radius * 2) - radius) + (int) this.world.getSpawnLocation().getZ();

        if (this.count < 500) {
            new Location(this.world, x, 1, z).getChunk().load(true);
            new WildernessCheckTask(this.player, this.start, this.world, x, z, this.count).runTaskLater(DoodCorePlugin.plugin, 1L);
        } else {
            StaticMethods.log("Wilderness teleport for " + this.player.getName() + " was cancelled.");
            this.player.sendMessage(StaticMethods.addColor("&cCould not find a suitable location to teleport you. Please try again."));
        }
    }
}