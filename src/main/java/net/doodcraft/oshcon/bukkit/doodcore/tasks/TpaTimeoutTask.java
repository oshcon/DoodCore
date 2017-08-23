package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.commands.TpaCommand;
import net.doodcraft.oshcon.bukkit.doodcore.commands.TpahereCommand;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class TpaTimeoutTask extends BukkitRunnable {

    private UUID uuid;

    public TpaTimeoutTask(UUID uuid, Long delay) {
        this.uuid = uuid;
        this.runTaskLater(DoodCorePlugin.plugin, delay);
    }

    @Override
    public void run() {

        if (TpaCommand.requesting.containsKey(uuid)) {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage("§cYour tpa request timed out.");
            }

            if (Bukkit.getPlayer(TpaCommand.requesting.get(uuid)) != null) {
                Bukkit.getPlayer(TpaCommand.requesting.get(uuid)).sendMessage("§7" + Bukkit.getPlayer(uuid).getName() + "'s tpa request timed out.");
            }

            TpaCommand.requesting.remove(uuid);
            return;
        }

        if (TpahereCommand.requesting.containsKey(uuid)) {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage("§cYour tpahere request timed out.");
            }

            if (Bukkit.getPlayer(TpahereCommand.requesting.get(uuid)) != null) {
                Bukkit.getPlayer(TpahereCommand.requesting.get(uuid)).sendMessage("§7" + Bukkit.getPlayer(uuid).getName() + "'s tpahere request timed out.");
            }

            TpahereCommand.requesting.remove(uuid);
        }
    }
}