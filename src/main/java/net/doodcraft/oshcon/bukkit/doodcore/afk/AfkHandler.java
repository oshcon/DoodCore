package net.doodcraft.oshcon.bukkit.doodcore.afk;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkHandler {

    public static Map<UUID, Integer> tasks = new ConcurrentHashMap<>();
    public static Map<UUID, Long> lastAction = new ConcurrentHashMap<>();
    public static Map<UUID, String> lastLocation = new ConcurrentHashMap<>();

    public static void addAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new AfkTask(player), 0L, 10L);
            if (!tasks.containsKey(uuid)) {
                tasks.put(uuid, task);
            } else {
                Bukkit.getScheduler().cancelTask(tasks.get(uuid));
                tasks.remove(uuid);
                tasks.put(uuid, task);
            }
        }
    }

    public static void removeAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (tasks.containsKey(uuid)) {
                Bukkit.getScheduler().cancelTask(tasks.get(uuid));
                tasks.remove(uuid);
            }
        }
    }

    public static String locString(Location loc) {
        return (loc.getWorld().getName() + "," + Math.ceil(loc.getX()) + "," + Math.ceil(loc.getY()) + "," + Math.ceil(loc.getZ()));
    }
}