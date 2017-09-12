package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CoreSignListener implements Listener {
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                if (StaticMethods.removeColor(sign.getLine(0)).equalsIgnoreCase("[Click Me]")) {
                    if (sign.getLine(1).equalsIgnoreCase("[Discord]")) {
                        Player player = event.getPlayer();
                        player.sendMessage("§8[§7Discord§8] §bInvite URL: https://discord.gg/" + StaticMethods.removeColor(sign.getLine(3)));
                    }
                }
            }
        }
    }
}