package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryChat;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import com.gmail.nossr50.api.ChatAPI;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.commands.BackCommand;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerMethods.loadCorePlayer(player);
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);
        PlayerMethods.dumpCorePlayer(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);
        String message = event.getMessage();

        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "");
        }

        event.getRecipients().clear();

        if (Compatibility.isHooked("MarriageMaster")) {
            MarryChat mc = new MarryChat((MarriageMaster) Compatibility.getPlugin("MarriageMaster"));
            if (mc.pcl.contains(player)) {
                return;
            }
        }

        if (Compatibility.isHooked("mcMMO")) {
            if (ChatAPI.isUsingPartyChat(player)) {
                return;
            }
            if (ChatAPI.isUsingAdminChat(player)) {
                return;
            }
        }

        if (Compatibility.isHooked("TownyChat")) {
            // TODO
        }

        if (!event.isCancelled()) {
            if (DiscordManager.toggled) {
                if (DiscordManager.client != null) {
                    // TODO: Allow @mentioning a user from in-game.
                    DiscordManager.sendGameChat(player, StaticMethods.removeColor(message));
                }
            }

            String msg;
            if (player.hasPermission("core.chat.colors")) {
                msg = StaticMethods.addColor(event.getMessage()).replaceAll("§k", "");
            } else {
                msg = StaticMethods.removeColor(event.getMessage());
            }

            DiscordManager.broadcastJson(player, msg);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
            Block block = event.getTo().add(0, 1, 0).getBlock();
            if (block != null) {
                if (block.getType().isSolid() || block.getType().isOccluding()) {
                    event.getPlayer().teleport(event.getTo().getWorld().getHighestBlockAt(event.getTo()).getLocation().add(0, 1, 0));
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
    public void onCommand(PlayerCommandPreprocessEvent event) {

        // I don't EVER want to see "magic" characters anywhere, ever again.
        event.setMessage(event.getMessage().replaceAll("&k", ""));

        String command = event.getMessage().split(" ")[0].toLowerCase().replaceAll("/", "");

        // check cooldowns
        if (!event.getPlayer().hasPermission("core.bypass.cooldowns")) {
            if (CommandCooldowns.hasCooldown(event.getPlayer().getUniqueId(), command)) {
                if (CommandCooldowns.getCooldown(event.getPlayer().getUniqueId(), command) > 0L) {
                    event.getPlayer().sendMessage(StaticMethods.addColor("&cYou must wait to use this command again. &8[&e" + (Math.ceil(CommandCooldowns.getCooldown(event.getPlayer().getUniqueId(), command.replaceAll("/", "")) / 1000)) + "s&8]"));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        CommandCooldowns.removeCooldown(event.getPlayer().getUniqueId(), command);

        if (command.equalsIgnoreCase("afk")) {
            return;
        }

        if (command.equalsIgnoreCase("mytime")) {
            return;
        }

        if (command.equalsIgnoreCase("vanish")) {
            return;
        }

        CorePlayer cPlayer = CorePlayer.getPlayers().get(event.getPlayer().getUniqueId());
        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "Using commands/chatting");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "");
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
                                }
                                return;
                            }
                        }
                    }

                    player.sendMessage("§cThat entity cannot be given to another player.");
                    waiting.remove(uuid);
                    requesting.remove(uuid);
                    CommandCooldowns.addCooldown(uuid, "givepet", 60000L);
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (!DiscordManager.toggled) {
            return;
        }

        BackCommand.addBackLocation(event.getEntity());

        DiscordManager.sendGameDeath(event.getEntity(), event.getDeathMessage());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(event.getPlayer().getUniqueId());

        for (String name : cPlayer.getHomes().keySet()) {
            if (name.equalsIgnoreCase("home")) {
                event.setRespawnLocation(StaticMethods.getPreciseLocationFromString(cPlayer.getHomes().get(name)));
                return;
            }
        }

        event.setRespawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
    }
}