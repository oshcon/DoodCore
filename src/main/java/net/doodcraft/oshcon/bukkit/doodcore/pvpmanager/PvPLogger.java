package net.doodcraft.oshcon.bukkit.doodcore.pvpmanager;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PvPLogger implements Listener {

    public static List<String> blockedCommands = new ArrayList<>();
    public static Map<UUID, Long> inCombat = new ConcurrentHashMap<>();
    public static Map<UUID, Integer> combatTasks = new ConcurrentHashMap<>();

    public static void setupBlockedCommands() {
        blockedCommands.add("home");
        blockedCommands.add("spawn");
        blockedCommands.add("wild");
        blockedCommands.add("tpa");
        blockedCommands.add("tpahere");
        blockedCommands.add("tpaccept");
        blockedCommands.add("t");
        blockedCommands.add("town");
        blockedCommands.add("towny");
        blockedCommands.add("ptp");
        blockedCommands.add("marry");
    }

    public static void runCombat(Player player) {

        Long started = System.currentTimeMillis();

        // Already in combat, update the last time.
        if (inCombat.containsKey(player.getUniqueId())) {
            inCombat.put(player.getUniqueId(), started);
            return;
        }

        // They've just triggered combat with another player.
        if (!inCombat.containsKey(player.getUniqueId())) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, (float) 2.0, (float) 1.4);
            player.sendMessage(StaticMethods.addColor("&cYou have entered mortal kombat!!\n&8:: &eTeleportation commands are disabled!\n&8:: &eLogging out before it's over &nwill&e result in DEATH!"));
            inCombat.put(player.getUniqueId(), started);
            final UUID uuid = player.getUniqueId();
            combatTasks.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    if (inCombat.containsKey(uuid)) {
                        if ((System.currentTimeMillis() - inCombat.get(uuid)) > 14000) {
                            if (Bukkit.getPlayer(uuid) != null) {
                                player.sendMessage(StaticMethods.addColor("&aYou are no longer in combat."));
                            }
                            inCombat.remove(uuid);
                            Bukkit.getScheduler().cancelTask(combatTasks.get(uuid));
                            combatTasks.remove(uuid);
                        }
                    }
                }
            }, 300L, 20L));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (inCombat.containsKey(event.getPlayer().getUniqueId())) {
            if (!event.getPlayer().isDead() && event.getPlayer().isOnline()) {
                Inventory inv = event.getPlayer().getInventory();

                for (ItemStack i : inv.getContents()) {
                    if (i != null) {
                        event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), i);
                    }
                }

                event.getPlayer().getInventory().clear();
                event.getPlayer().setHealth(0.0);

                Bukkit.broadcastMessage(StaticMethods.addColor("&7" + event.getPlayer().getName() + " &7logged out during PvP and paid the ultimate price! :o"));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) {
            return;
        }

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player aggressor = null;

            if (event.getDamager() instanceof EnderPearl) {
                EnderPearl pearl = (EnderPearl) event.getDamager();
                if (pearl.getShooter() instanceof Player) {
                    aggressor = (Player) pearl.getShooter();
                }
            }

            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    aggressor = (Player) arrow.getShooter();
                }
            }

            if (event.getDamager() instanceof SplashPotion) {
                SplashPotion potion = (SplashPotion) event.getDamager();
                if (potion.getShooter() instanceof Player) {
                    aggressor = (Player) potion.getShooter();
                }
            }

            if (event.getDamager() instanceof LingeringPotion) {
                LingeringPotion potion = (LingeringPotion) event.getDamager();
                if (potion.getShooter() instanceof Player) {
                    aggressor = (Player) potion.getShooter();
                }
            }

            if (event.getDamager() instanceof Player) {
                aggressor = (Player) event.getDamager();
            }

            if (aggressor != null) {

                if (aggressor.equals(victim)) {
                    return;
                }

                // Cancel attack based on certain conditions here:
                CorePlayer cPlayerVictim = CorePlayer.getPlayers().get(victim.getUniqueId());
                CorePlayer cPlayerAggressor = CorePlayer.getPlayers().get(aggressor.getUniqueId());

                if (cPlayerVictim != null) {
                    if (cPlayerVictim.getCurrentActiveTime() < (Settings.pvpProtection * 1000)) {
                        event.setCancelled(true);
                        int timeLeft = Math.toIntExact((Settings.pvpProtection * 1000) - cPlayerVictim.getCurrentActiveTime());
                        aggressor.sendMessage("§cThey still have new player PvP protection. §8[§e" + timeLeft + "s§8]");
                        return;
                    }
                }

                if (cPlayerAggressor != null) {
                    if (cPlayerAggressor.getCurrentActiveTime() < (Settings.pvpProtection * 1000)) {
                        event.setCancelled(true);
                        int timeLeft = Math.toIntExact((Settings.pvpProtection * 1000) - cPlayerAggressor.getCurrentActiveTime());
                        aggressor.sendMessage("§cYou cannot PvP during new player protection. §8[§e" + timeLeft + "s§8]");
                        return;
                    }
                }

                // Notify them of their combat status and start blocking commands.
                runCombat(victim);
                runCombat(aggressor);
            }
        }
    }
}
