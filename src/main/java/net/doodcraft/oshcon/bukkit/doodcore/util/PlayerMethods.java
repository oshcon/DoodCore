package net.doodcraft.oshcon.bukkit.doodcore.util;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import org.bukkit.entity.Player;

public class PlayerMethods {
    public static Boolean hasPermission(Player player, String node, Boolean sendError) {
        if (player.isOp()) {
            return true;
        }

        if (player.hasPermission(DoodCorePlugin.plugin.getName().toLowerCase() + ".*")) {
            return true;
        }

        if (player.hasPermission(node)) {
            return true;
        }

        if (sendError) {
            player.sendMessage("Unknown command. Type \"help\" for help.");
        }

        return false;
    }
}
