package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityListener implements Listener {

    // Cancel fall damage for rabbits, because they suicide too often. :(
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Rabbit) {
            if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);
            }
        }
    }
}
