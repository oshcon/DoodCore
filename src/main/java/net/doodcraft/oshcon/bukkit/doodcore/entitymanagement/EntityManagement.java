package net.doodcraft.oshcon.bukkit.doodcore.entitymanagement;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import org.bukkit.Bukkit;

public class EntityManagement {
    public static void startItemPurgeTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new PurgeItemTask(), 0L, 1800*20L);
    }
}
