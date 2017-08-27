package net.doodcraft.oshcon.bukkit.doodcore.badges;

import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BadgeAwardEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CorePlayer cPlayer;
    private Badge badge;

    public BadgeAwardEvent(CorePlayer cPlayer, Badge badge) {
        this.cPlayer = cPlayer;
        this.badge = badge;
    }

    public CorePlayer getCorePlayer() {
        return this.cPlayer;
    }

    public Badge getBadge() {
        return this.badge;
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
