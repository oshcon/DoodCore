package net.doodcraft.oshcon.bukkit.doodcore.util;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TownDistance {

    public static boolean checkTownDistance(Player player, Integer radius) {
        if (player.hasPermission("doodcore.admin.bypass")) {
            return false;
        } else {
            if (!startDistanceCheck(player, radius)) {
                player.sendMessage("You are too close to another town to use this.");
                return true;
            }
            return false;
        }
    }

    public static boolean startDistanceCheck(final Player player, final Integer radius) {

        if (Compatibility.isHooked("Towny")) {

            Location loc = player.getLocation();

            String townName = null;
            Resident resident;
            Town town = null;

            try {
                resident = TownyUniverse.getDataSource().getResident(player.getName());

                if (resident.hasTown()) {
                    town = resident.getTown();
                    townName = town.getName();
                } else {
                    townName = "Wilderness";
                }
            } catch (Exception ex) {
                townName = "Wilderness";
                StaticMethods.log("TownyNotRegisteredException");
            }

            try {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if ((x * x) + (z * z) <= (radius * radius)) {
                            Location check = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY(), loc.getBlockZ() + z);
                            Block block = check.getBlock();

                            String otherTownName = TownyUniverse.getTownName(check);

                            Town otherTown = null;
                            try {
                                try {
                                    otherTown = TownyUniverse.getDataSource().getTown(otherTownName);
                                } catch (NullPointerException ex) {
                                    otherTownName = "Wilderness";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (!TownyUniverse.isWilderness(block) && !townName.equalsIgnoreCase(otherTownName)) {
                                return false;
                            }

                            if (otherTown != null && !CombatUtil.isAlly(otherTown, town)) {
                                return false;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        } else {
            return true;
        }
        return true;
    }
}
