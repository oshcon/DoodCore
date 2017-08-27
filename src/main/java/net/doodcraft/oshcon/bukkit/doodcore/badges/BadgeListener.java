package net.doodcraft.oshcon.bukkit.doodcore.badges;

import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BadgeListener implements Listener {
    @EventHandler
    public void onBadgeAward(BadgeAwardEvent event) {
        if (event.getBadge() != null) {
            Badge b = event.getBadge();
            FancyMessage msg = new FancyMessage("§7" + event.getPlayer().getName() + " earned the badge ");
            msg.then("§8[" + b.getFriendlyName() + "§8]").tooltip(b.getDescription());
            msg.then(" §8[§b/badges§8]");
            for (Player p : Bukkit.getOnlinePlayers()) {
                msg.send(p);
            }
        }
    }
}