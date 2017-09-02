package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryChat;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import com.gmail.nossr50.api.ChatAPI;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.pvpmanager.PvPLogger;
import net.doodcraft.oshcon.bukkit.doodcore.util.CommandCooldowns;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {

        // I don't EVER want to see "magic" characters anywhere, ever again.
        event.setMessage(event.getMessage().replaceAll("&k", ""));

        String command = event.getMessage().split(" ")[0].toLowerCase().replaceAll("/", "");

        if (PvPLogger.inCombat.containsKey(event.getPlayer().getUniqueId())) {
            for (String c : PvPLogger.blockedCommands) {
                if (command.equalsIgnoreCase(c)) {
                    event.getPlayer().sendMessage(StaticMethods.addColor("&cYou cannot use that command during PvP."));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // check cooldowns
        if (!event.getPlayer().hasPermission("core.bypass.cooldowns")) {
            if (CommandCooldowns.hasCooldown(event.getPlayer().getUniqueId(), command)) {
                if (CommandCooldowns.getCooldown(event.getPlayer().getUniqueId(), command) > 0L) {
                    event.getPlayer().sendMessage(StaticMethods.addColor("&cYou must wait to use this command again. &8[&e" + ((int) Math.ceil(CommandCooldowns.getCooldown(event.getPlayer().getUniqueId(), command.replaceAll("/", "")) / 1000)) + "s&8]"));
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

        if (command.equalsIgnoreCase("vote")) {
            return;
        }

        if (command.equalsIgnoreCase("vanish")) {
            return;
        }

        CorePlayer cPlayer = CorePlayer.getPlayers().get(event.getPlayer().getUniqueId());
        if (cPlayer != null) {
            if (!cPlayer.isVanished()) {
                cPlayer.setAfkStatus(false, "Using commands/chatting");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);
        String message = event.getMessage();

        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "");
        }

        // Check if another plugin modifies the recipient list, if not, it should match the online count.
        if (event.getRecipients().size() != Bukkit.getOnlinePlayers().size()) {
            return;
        }

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

        event.getRecipients().clear();

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
}
