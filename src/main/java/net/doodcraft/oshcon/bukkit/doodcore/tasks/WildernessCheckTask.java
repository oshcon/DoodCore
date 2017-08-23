package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WildernessCheckTask extends BukkitRunnable {

    Player player;
    Location start;
    World world;
    int x;
    int z;
    int count;

    public WildernessCheckTask(Player player, Location start, World world, int x, int z, int count) {
        this.player = player;
        this.start = start;
        this.world = world;
        this.x = x;
        this.z = z;
        this.count = count;
    }

    @Override
    public void run() {
        if (!this.player.isOnline() || this.player.isDead()) {
            this.cancel();
            return;
        }

        int y = searchSuitableYLevel(this.world, this.x, this.z);

        if (y >= 0) {
            Location to = new Location(this.world, x + 0.5, y + 1, z + 0.5);

            if (!PlayerMethods.canBuild(this.player, to.getBlock())) {
                y = -1;
            }

            if (y <= -1) {
                if (count < 500) {
                    new WildernessSearchTask(this.player, this.count + 5).runTask(DoodCorePlugin.plugin);
                } else {
                    StaticMethods.log("Wilderness teleport for " + this.player.getName() + " was cancelled.");
                    this.player.sendMessage(StaticMethods.addColor("&cCould not find a suitable location to teleport you. Please try again."));
                }
            } else {
                StaticMethods.log("&7Teleporting " + player.getName() + " into the wilderness at &8[&e" + (int) to.getX() + " " + (int) to.getY() + " " + (int) to.getZ() + "&8]");
                String message = "&7Teleported you to &8[&e" + (int) to.getX() + " " + (int) to.getY() + " " + (int) to.getZ() + "&8]";
                new WarmupTeleportTask(player, to, null, message, "wild", 5000);
            }
        } else {
            if (count < 500) {
                new WildernessSearchTask(this.player, this.count + 5).runTask(DoodCorePlugin.plugin);
            } else {
                StaticMethods.log("Wilderness teleport for " + this.player.getName() + " was cancelled.");
                this.player.sendMessage(StaticMethods.addColor("&cCould not find a suitable location to teleport you. Please try again."));
            }
        }
    }

    // This method search for a safe y level at the specified world, x, z location
    // It uses a custom algorithm that tries to skip empty blocks (fastforward)
    // CREDIT: KaiKikuchi (https://github.com/KaiKikuchi)
    @SuppressWarnings("deprecation")
    public int searchSuitableYLevel(World world, int x, int z) {
        int y = 126; // Max y level
        int fastforward = 8; // default fastforward
        if (world.getEnvironment() == World.Environment.THE_END) { //
            fastforward = 2; // fastforward for the end
        }

        Block block = world.getBlockAt(x, y, z);
        int c, solid = Integer.MAX_VALUE, shift = 0;
        do {
            if (block.getType().isSolid()) {
                if (shift == fastforward && solid > y) { // Stop fastforward
                    solid = y;
                    y += fastforward - 1;
                    block = block.getRelative(BlockFace.UP, fastforward - 1);
                    continue;
                } else {
                    Block upperBlock = block.getRelative(BlockFace.UP);

                    c = 0;
                    while (c < 4) { // a y level is suitable if there are 4 empty blocks above a valid solid block
                        if (upperBlock.getTypeId() != 0) { // if it's not empty
                            if (upperBlock.isLiquid()) { // if you get a liquid, it could be an ocean or a lava lake... there won't be any good y level here.
                                return -1;
                            }
                            break;
                        }
                        upperBlock = upperBlock.getRelative(BlockFace.UP);
                        c++;
                    }

                    if (c == 4) {
                        return y; // Found a suitable y level
                    } else {
                        shift = 4; //
                    }
                }
            } else {
                if (solid > y) { // fastforward mode
                    shift = fastforward;
                } else {
                    shift = 1;
                }
            }

            y -= shift;
            block = block.getRelative(BlockFace.DOWN, shift);
        } while (y > 0);

        return -1;
    }
}