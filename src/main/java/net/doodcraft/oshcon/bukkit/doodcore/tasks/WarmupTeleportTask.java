package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import de.slikey.effectlib.effect.SphereEffect;
import de.slikey.effectlib.effect.WarpEffect;
import de.slikey.effectlib.util.DynamicLocation;
import de.slikey.effectlib.util.ParticleEffect;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.commands.BackCommand;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class WarmupTeleportTask extends BukkitRunnable {

    public static ArrayList<UUID> teleporting = new ArrayList<>();

    private Player player;
    private Location from, to;
    private Entity toEnt;
    private UUID uuid;
    private String arrived;
    private String command;

    public WarmupTeleportTask(Player player, Location to, Entity toEnt, String arrived, String command, Integer length) {
        this.player = player;
        this.from = this.player.getLocation();
        this.to = to;
        this.toEnt = toEnt;
        this.uuid = this.player.getUniqueId();
        this.arrived = arrived;
        this.command = command;

        try {
            if (player.hasPermission("core.bypass.warmpups")) {
                Location from = player.getLocation();
                player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                playTeleportEffects(player, from, to);
                player.sendMessage(StaticMethods.addColor(arrived));

                if (this.command.equalsIgnoreCase("back")) {
                    if (BackCommand.deathLocations.containsKey(player.getUniqueId())) {
                        BackCommand.deathLocations.remove(player.getUniqueId());
                    }
                }
                return;
            }

            this.player.sendTitle(StaticMethods.addColor("&7Teleporting..."), StaticMethods.addColor("&8[&cDo not move&8]"), 16, ((length / 1000) - 2) * 20, 16);
            teleporting.add(this.uuid);
            playWarmupEffects(player, length);
            this.runTaskLater(DoodCorePlugin.plugin, ((length / 1000) * 20));
        } catch (Exception ex) {
            StaticMethods.log("There was an error teleporting " + player.getName());
            player.sendMessage("Your teleport was cancelled due to an unknown error.\nPlease report this to staff asap.");
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!this.player.isOnline() || this.player.isDead()) {
            StaticMethods.log("Cancelled teleport for offline/dead player " + player.getName());
            teleporting.remove(this.uuid);
            this.cancel();
            return;
        }

        if (this.player.getLocation().distanceSquared(from) >= 2) {
            StaticMethods.log("Cancelled teleport due to movement for " + player.getName());
            if (command.equalsIgnoreCase("tpaccept")) {
                if (toEnt instanceof Player) {
                    Player player = (Player) toEnt;
                    player.sendMessage(StaticMethods.addColor("&c" + player.getName() + " moved, and could not be teleported."));
                }
            }
            this.player.sendMessage(StaticMethods.addColor("&cCancelled teleported due to movement."));
            teleporting.remove(this.uuid);
            this.cancel();
            return;
        }

        Location from = player.getLocation();
        this.player.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
        playTeleportEffects(player, from, to);
        this.player.sendMessage(StaticMethods.addColor(this.arrived));

        teleporting.remove(this.uuid);

        if (this.command.equalsIgnoreCase("back")) {
            if (BackCommand.deathLocations.containsKey(player.getUniqueId())) {
                BackCommand.deathLocations.remove(player.getUniqueId());
            }
        }

        if (this.command.equalsIgnoreCase("spawn")) {
            CommandCooldowns.addCooldown(player.getUniqueId(), "spawn", 60000L);
        }

        if (this.command.equalsIgnoreCase("wild")) {
            CommandCooldowns.addCooldown(player.getUniqueId(), "wild", 180000L);
        }
    }

    public static void playWarmupEffects(Player player, int length) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (!cPlayer.isVanished()) {
            Location loc = player.getLocation();
            SphereEffect loading = new SphereEffect(DoodCorePlugin.effectManager);
            loading.duration = length;
            loading.autoOrient = true;
            loading.iterations = 10;
            loading.radius = 0.25;
            loading.radiusIncrease = 0.0013;
            loading.particle = ParticleEffect.PORTAL;
            loading.particles = 3;
            loading.setLocation(loc);
            loading.setDynamicTarget(new DynamicLocation(loc, player));
            loading.disappearWithTargetEntity = true;
            loading.visibleRange = 64F;
            loading.start();
        }
    }

    public static void playTeleportEffects(Player player, Location from, Location to) {

        player.setFallDistance(-100);
        player.setNoDamageTicks(200);
        player.setRemainingAir(20);

        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (!cPlayer.isVanished()) {
            // FROM
            WarpEffect warpEffectFrom = new WarpEffect(DoodCorePlugin.effectManager);
            warpEffectFrom.particle = ParticleEffect.SPELL_WITCH;
            warpEffectFrom.radius = 0.72F;
            warpEffectFrom.particles = 8;
            warpEffectFrom.rings = 1;
            warpEffectFrom.setLocation(from.add(0, 1, 0));
            warpEffectFrom.start();
            player.getLocation().getWorld().playSound(from, Sound.ENTITY_ENDERMEN_TELEPORT, 1F, 1.35F);

            // TO
            WarpEffect warpEffectTo = new WarpEffect(DoodCorePlugin.effectManager);
            warpEffectTo.particle = ParticleEffect.SPELL_WITCH;
            warpEffectTo.radius = 0.72F;
            warpEffectTo.particles = 8;
            warpEffectTo.rings = 1;
            warpEffectTo.setLocation(to.add(0.0, 1, 0.0));
            warpEffectTo.start();
            player.playEffect(to, Effect.ENDER_SIGNAL, null);
            to.getWorld().playSound(to, Sound.ENTITY_GHAST_SHOOT, 2F, 1.5F);
        }
    }
}