package net.doodcraft.oshcon.bukkit.doodcore.util;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.DiscordUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;

public class PlayerMethods {

    public static UUID getCrackedUUID(String player) {
        try {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isOffHandClick(PlayerInteractEvent event) {
        return event.getHand().equals(EquipmentSlot.valueOf("OFF_HAND"));
    }

    public static boolean isOffHandClick(PlayerInteractAtEntityEvent event) {
        return event.getHand().equals(EquipmentSlot.valueOf("OFF_HAND"));
    }

    public static String getPrimaryGroup(Player player) {
        if (Compatibility.isHooked("Vault") && Vault.chat != null && Vault.chat.isEnabled()) {
            try {
                return Vault.chat.getPrimaryGroup(null, player);
            } catch (Exception ex) {
                return "Member";
            }
        } else {
            return "Member";
        }
    }

    public static String getPlayerPrefix(Player player) {
        if (Compatibility.isHooked("Vault") && Vault.chat != null && Vault.chat.isEnabled()) {
            try {
                return Vault.chat.getPlayerPrefix(null, player);
            } catch (Exception ex) {
                return "§e";
            }
        } else {
            return "§e";
        }
    }

    public static String getChatPrefix(Player player) {
        if (Compatibility.isHooked("Vault") && Vault.chat != null && Vault.chat.isEnabled()) {
            try {
                String pGroup = Vault.permission.getPrimaryGroup(null, player);
                if (pGroup.equalsIgnoreCase("Member")) {
                    return "§8[§eMember§8]§r";
                }
                if (pGroup.equalsIgnoreCase("Veteran")) {
                    return "§8[§2Veteran§8]§r";
                }
                if (pGroup.equalsIgnoreCase("Trainee")) {
                    return "§8[§bTrainee§8]§r";
                }
                if (pGroup.equalsIgnoreCase("Artist")) {
                    return "§8[§dArtist§8]§r";
                }
                if (pGroup.equalsIgnoreCase("Bouncer")) {
                    return "§8[§3Bouncer§8]§r";
                }
                if (pGroup.equalsIgnoreCase("Admin")) {
                    return "§8[§5Admin§8]§r";
                }
            } catch (Exception ex) {
                return "§8[§eMember§8]§r";
            }
        }

        return "§8[§eMember§8]§r";
    }

    public static Boolean canBuild(Player player, Block block) {
        if (block != null) {
            BlockBreakEvent event = new BlockBreakEvent(block, player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                event.setCancelled(true);
                return false;
            } else {
                event.setCancelled(true);
                return true;
            }
        }
        return true;
    }

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
            player.sendMessage("§cNo permission.");
        }

        return false;
    }

    public static Boolean hasVotedToday(Player player) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        return (System.currentTimeMillis() - cPlayer.getLastVote()) <= 86400 * 1000;
    }

    public static Boolean isSupporter(Player player) {
        return Arrays.toString(Vault.permission.getPlayerGroups(null, player.getPlayer())).contains("Supporter");
    }

    public static void dumpAllCorePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            dumpCorePlayer(player);
        }
    }

    public static void loadAllCorePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadCorePlayer(player);
        }
    }

    public static void dumpCorePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);

        new DiscordUpdateTask().runTaskAsynchronously(DoodCorePlugin.plugin);

        cPlayer.setLastQuit(System.currentTimeMillis());
        cPlayer.setLastLocation(StaticMethods.getLocString(cPlayer.getPlayer().getLocation()));
        cPlayer.destroy();
    }

    public static CorePlayer loadCorePlayer(Player player) {
        CorePlayer.createCorePlayer(player);
        UUID uuid = player.getUniqueId();

        return CorePlayer.getPlayers().get(uuid);
    }
}