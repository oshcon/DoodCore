package net.doodcraft.oshcon.bukkit.doodcore.entitymanagement;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.PurgeItemTask;
import org.bukkit.Bukkit;

public class EntityManagement {
    public static void startItemPurgeTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new PurgeItemTask(), 0L, 3600 * 20L);
    }
}
