package net.doodcraft.oshcon.bukkit.doodcore.pvpmanager;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.commands.BackCommand;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
                dropInventory(event.getPlayer());
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
                        int timeLeft = Math.toIntExact(((Settings.pvpProtection * 1000) - cPlayerVictim.getCurrentActiveTime())/1000);
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

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() != null) {
            if (event.getEntity() instanceof LivingEntity) {
                if (event.getEntity() instanceof Player) {
                    return;
                }

                if (event.getEntity().getKiller() != null) {
                    if (event.getEntity().getKiller() instanceof Player) {
                        Player killer = event.getEntity().getKiller();
                        CorePlayer cKiller = CorePlayer.getPlayers().get(killer.getUniqueId());
                        cKiller.addKill(event.getEntity());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        // Already in combat, update the last time.
        if (inCombat.containsKey(event.getEntity().getUniqueId())) {
            inCombat.put(event.getEntity().getUniqueId(), System.currentTimeMillis() - 14001L);
        }

        if (event.getEntity().getKiller() != null) {
            if (event.getEntity().getKiller() instanceof Player) {
                Player killer = event.getEntity().getKiller();
                CorePlayer cKiller = CorePlayer.getPlayers().get(killer.getUniqueId());
                dropInventory(event.getEntity());
                dropHead(event.getEntity(), cKiller);
                cKiller.addKill(event.getEntity());

                event.getEntity().sendMessage("§cYou dropped your inventory due to PvP.");
                // Add a method to drop player heads.
                return;
            }
        }

        BackCommand.addBackLocation(event.getEntity());
    }

    public static void dropInventory(Player player) {
        Inventory inv = player.getInventory();

        for (ItemStack i : inv.getContents()) {
            if (i != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), i);
            }
        }

        player.getInventory().clear();
    }

    public static void dropHead(Player player, CorePlayer cKiller) {
        if (cKiller.getKills().size() > 1) {
            if (!cKiller.getKills().containsKey("Player:" + player.getName())) {
                ItemStack item = new ItemStack(Material.SKULL_ITEM, (short) 3);
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwner(player.getName());
                meta.setDisplayName("§r" + player.getName() + "'s Head");
                item.setItemMeta(meta);
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                Bukkit.broadcastMessage("§3" + cKiller.getName() + " beheaded " + player.getName() + "! :o");
            }
        }
    }
}