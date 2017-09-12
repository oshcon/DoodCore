package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.MinecraftMessages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VanishListener implements Listener {

    @EventHandler
    public void onVanish(PlayerHideEvent event) {
        MinecraftMessages.broadcastQuit(CorePlayer.getPlayers().get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onUnVanish(PlayerShowEvent event) {
        MinecraftMessages.broadcastJoin(CorePlayer.getPlayers().get(event.getPlayer().getUniqueId()));
    }
}
