package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.commands.TrackCommand;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordMessages;
import net.doodcraft.oshcon.bukkit.doodcore.discord.MinecraftMessages;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.PlayerUpdateTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        CorePlayer cPlayer = PlayerMethods.loadCorePlayer(player);

        event.setJoinMessage(null);
        String login = Messages.parse(cPlayer, "§8[§7<time>§8] §7<roleprefix><name> §7joined §e§oBending§7.");
        player.sendMessage(login);

        new PlayerUpdateTask(player.getUniqueId()).runTaskTimer(DoodCorePlugin.plugin, 60L, 20L);

        if (!player.hasPlayedBefore()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player)) {
                    p.sendMessage("§8§m--§8[ §bWelcome " + cPlayer.getColorPrefix() + cPlayer.getNick() + "§b to DoodCraft! §8]§m--");
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

        if (cPlayer.isCurrentlyAfk()) {
            cPlayer.setAfkStatus(false, "Quitting");
        }

        TrackCommand.resetCompass(player);

        event.setQuitMessage(null);

        if (AuthMeApi.getInstance().isAuthenticated(cPlayer.getPlayer())) {
            MinecraftMessages.broadcastQuit(cPlayer);
        }

        PlayerMethods.dumpCorePlayer(player);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack flare = event.getItemInHand();
        if (flare.hasItemMeta()) {
            if (flare.getItemMeta().hasDisplayName()) {
                if (flare.getItemMeta().getDisplayName().equals("§3§lVote Flare")) {
                    if (event.getBlockPlaced().getRelative(BlockFace.DOWN).equals(event.getBlockAgainst())) {
                        // It was placed on the ground
                        if (!VotifierListener.runFlareReward(flare, event.getBlockPlaced().getLocation(), CorePlayer.getPlayers().get(event.getPlayer().getUniqueId()))) {
                            event.getPlayer().sendMessage("§cThe flare cannot be used here.");
                            event.setCancelled(true);
                        }
                    } else {
                        // It was placed on a wall
                        event.getPlayer().sendMessage("§cThe flare must be placed on the ground.");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChange(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("core.chat.colors")) {
            return;
        }

        int n = 0;
        while (n <= 3) {
            event.setLine(n, StaticMethods.addColor(event.getLine(n)));
            n++;
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

        if (cPlayer != null) {
            if (!cPlayer.isVanished()) {
                cPlayer.setAfkStatus(false, "Interacting");
            }
        }
    }

    // Waiting for these people to click a pet.. *looks at watch*
    public static HashMap<UUID, Long> waiting = new HashMap<>();

    // Key UUID is sending their pet to Value UUID
    public static HashMap<UUID, UUID> requesting = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (PlayerMethods.isOffHandClick(event)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {

            @Override
            public void run() {
                if (waiting.containsKey(uuid)) {
                    Entity entity = event.getRightClicked();
                    if (entity instanceof Tameable) {
                        Tameable tameable = (Tameable) entity;
                        if (tameable.isTamed()) {
                            AnimalTamer tamer = tameable.getOwner();
                            if (uuid == tamer.getUniqueId()) {
                                event.setCancelled(true);

                                Player newOwner = Bukkit.getPlayer(requesting.get(uuid));
                                if (entity instanceof Wolf) {
                                    Wolf wolf = (Wolf) entity;
                                    wolf.setSitting(false);
                                    wolf.setOwner(newOwner);
                                    wolf.teleport(newOwner);
                                    newOwner.sendMessage("§7Someone sent you §b" + entity.getCustomName() + " §7the dog.");
                                    player.sendMessage("§7You sent §b" + entity.getCustomName() + " §7the dog to " + newOwner.getName() + ".");
                                    waiting.remove(uuid);
                                    requesting.remove(uuid);
                                    CommandCooldowns.addCooldown(player.getUniqueId(), "givepet", 30000L);
                                }
                                if (entity instanceof Ocelot) {
                                    Ocelot o = (Ocelot) entity;
                                    o.setSitting(false);
                                    o.setOwner(newOwner);
                                    o.teleport(newOwner);
                                    newOwner.sendMessage("§7Someone sent you §b" + entity.getCustomName() + " §7the cat.");
                                    player.sendMessage("§7You sent §b" + entity.getCustomName() + " §7the cat to " + newOwner.getName() + ".");
                                    waiting.remove(uuid);
                                    requesting.remove(uuid);
                                    CommandCooldowns.addCooldown(player.getUniqueId(), "givepet", 30000L);
                                }
                                return;
                            }
                        }
                    }

                    player.sendMessage("§cThat entity cannot be given to another player.");
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (!DiscordManager.toggled) {
            return;
        }

        DiscordMessages.sendGameDeath(event.getEntity(), event.getDeathMessage());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(event.getPlayer().getUniqueId());

        if (PlayerMethods.hasVotedToday(event.getPlayer())) {
            for (String name : cPlayer.getHomes().keySet()) {
                if (name.equalsIgnoreCase("home")) {
                    event.setRespawnLocation(StaticMethods.getPreciseLocationFromString(cPlayer.getHomes().get(name)));
                    return;
                }
            }
        }

        event.setRespawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() != null) {
            Material mat = block.getType();

            if (player != null) {
                if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                    if (mat.equals(Material.LEAVES) || mat.equals(Material.LEAVES_2)) {
                        int chance = DoodCorePlugin.random.nextInt(23) + 1;

                        if (chance == 1) {
                            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.STICK, 1));
                        }

                        if (chance == 2) {
                            player.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.STICK, 2));
                        }
                    }
                }
            }
        }
    }
}