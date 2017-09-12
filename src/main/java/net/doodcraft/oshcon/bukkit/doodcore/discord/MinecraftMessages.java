package net.doodcraft.oshcon.bukkit.doodcore.discord;

import mkremins.fanciful.FancyMessage;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MinecraftMessages {

    public static void broadcastJoin(CorePlayer cPlayer) {
        // Broadcast to everyone.
        if (!cPlayer.isVanished()) {
            String login = Messages.parse(cPlayer, "§7<roleprefix><name> §7joined §e§oBending§7.");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!cPlayer.getPlayer().equals(p)) {
                    sendPlainMessage(p, login);
                }
            }
            // Broadcast to Discord
            if (DiscordManager.toggled) {
                if (!cPlayer.isIgnoringDiscord()) {
                    DiscordMessages.sendGameLogin(cPlayer.getPlayer());
                }
            }
        }
    }

    public static void broadcastQuit(CorePlayer cPlayer) {
        // Broadcast to everyone
        if (!cPlayer.isVanished()) {
            String quit = Messages.parse(cPlayer, "§7<roleprefix><name> §7left the game.");
            broadcastPlainMessage(quit);
            if (DiscordManager.toggled) {
                if (!cPlayer.isIgnoringDiscord()) {
                    DiscordMessages.sendGameQuit(cPlayer.getPlayer());
                }
            }
        }
    }

    public static void broadcastFancyChat(Player player, String message) {
        // Schedule this as a sync task, since we are potentially using Bukkit.dispatchCommand() or other Bukkit API calls async.
        Bukkit.getScheduler().runTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                FancyMessage msg = new FancyMessage(Messages.parse(cPlayer, "<chatprefix> §r<roleprefix><nick>"));

                String hover = Messages.parse(cPlayer, Messages.getHover(cPlayer));
                msg.tooltip(hover);
                msg.then("§8: §r");
                msg.link(message);

                String[] part = message.split(" ");

                // If there is a mention, ping the player, then send the FancyMessage.
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String name = p.getName();
                    String nick = StaticMethods.removeColor(cPlayer.getNick());

                    for (String s : part) {
                        if (s.startsWith("@")) {
                            String n = s.replaceAll("@", "");
                            if (n.equalsIgnoreCase(name) || n.equalsIgnoreCase(nick)) {
                                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.2F);
                            }
                        }
                    }

                    msg.send(p);
                }
            }
        });
    }

    public static void broadcastFancyMessage(FancyMessage msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendFancyMessage(p, msg);
        }
    }

    public static void broadcastPlainMessage(String string) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendPlainMessage(p, string);
        }
    }

    public static void broadcastDiscordMessage(String message) {
        for (CorePlayer cPlayer : CorePlayer.getPlayers().values()) {
            if (!cPlayer.isIgnoringDiscord()) {

                // Check for mentions from Discord to the server.
                String[] part = message.split(" ");
                for (String s : part) {
                    if (s.startsWith("@")) {
                        if (s.replaceAll("@", "").equalsIgnoreCase(cPlayer.getName())) {
                            cPlayer.getPlayer().playSound(cPlayer.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.2F);

                        }
                        if (s.replaceAll("@", "").equalsIgnoreCase(StaticMethods.removeColor(cPlayer.getNick()))) {
                            cPlayer.getPlayer().playSound(cPlayer.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 0.2F);
                        }
                    }
                }
                sendPlainMessage(cPlayer.getPlayer(), message);
            }
        }
    }

    public static void sendFancyMessage(Player player, FancyMessage msg) {
        msg.send(player);
    }

    public static void sendPlainMessage(Player player, String string) {
        player.sendMessage(StaticMethods.addColor(string));
    }
}