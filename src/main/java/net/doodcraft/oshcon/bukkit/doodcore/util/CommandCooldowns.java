package net.doodcraft.oshcon.bukkit.doodcore.util;

import java.util.HashMap;
import java.util.UUID;

public class CommandCooldowns {

    public static HashMap<UUID, HashMap<String, Long>> cooldownTimes = new HashMap<>();

    public static void removeAllCooldowns(UUID uuid) {
        if (cooldownTimes.containsKey(uuid)) {
            cooldownTimes.remove(uuid);
        }
    }

    public static Long getCooldown(UUID uuid, String command) {
        if (cooldownTimes.containsKey(uuid)) {
            HashMap<String, Long> current = cooldownTimes.get(uuid);
            if (current.containsKey(command)) {
                return (Long) current.get(command) - System.currentTimeMillis();
            }
        }
        return 0L;
    }

    public static boolean hasCooldown(UUID uuid, String command) {
        if (cooldownTimes.containsKey(uuid)) {
            HashMap<String, Long> current = cooldownTimes.get(uuid);
            if (current.containsKey(command)) {
                if (current.get(command) > 0L) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void addCooldown(UUID uuid, String command, Long ms) {
        if (cooldownTimes.containsKey(uuid)) {
            HashMap<String, Long> current = cooldownTimes.get(uuid);
            current.put(command, ms + System.currentTimeMillis());
            cooldownTimes.put(uuid, current);
        } else {
            HashMap<String, Long> current = new HashMap<>();
            current.put(command, ms + System.currentTimeMillis());
            cooldownTimes.put(uuid, current);
        }
    }

    public static void removeCooldown(UUID uuid, String command) {
        if (cooldownTimes.containsKey(uuid)) {
            HashMap<String, Long> current = cooldownTimes.get(uuid);
            current.remove(command);
            cooldownTimes.put(uuid, current);
        }
    }
}