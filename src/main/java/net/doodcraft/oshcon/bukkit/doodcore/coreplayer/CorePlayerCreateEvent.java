package net.doodcraft.oshcon.bukkit.doodcore.coreplayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CorePlayerCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CorePlayer cPlayer;

    public CorePlayerCreateEvent(CorePlayer cPlayer) {
        this.cPlayer = cPlayer;
    }

    public CorePlayer getCorePlayer() {
        return this.cPlayer;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(cPlayer.getUniqueId());
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
