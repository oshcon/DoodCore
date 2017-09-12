package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class PurgeItemTask implements Runnable {

    @Override
    public void run() {
        // announce at 3 minutes till
        Bukkit.getScheduler().scheduleSyncDelayedTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                if (Settings.purgeItems) {
                    Bukkit.broadcastMessage("§7Item drops will be removed in §b3 §7minutes.");
                }
            }
        }, 1620 * 20L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                if (Settings.purgeItems) {
                    int purged = 0;
                    for (World world : Bukkit.getWorlds()) {
                        purged = purged + StaticMethods.purgeItemDrops(world);
                    }

                    if (purged == 1) {
                        Bukkit.broadcastMessage("§7Removed §b" + purged + " §7item drop from the world.");
                    } else {
                        Bukkit.broadcastMessage("§7Removed §b" + purged + " §7item drops from the world.");
                    }
                }
            }
        }, 1800 * 20L);
    }
}