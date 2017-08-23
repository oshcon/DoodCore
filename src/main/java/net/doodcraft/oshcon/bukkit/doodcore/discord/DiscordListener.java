package net.doodcraft.oshcon.bukkit.doodcore.discord;

import net.doodcraft.oshcon.bukkit.doodcore.commands.DiscordCommand;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class DiscordListener {

    @EventSubscriber
    public synchronized void onMessage(MessageReceivedEvent event) {

        if (!DiscordManager.toggled) {
            return;
        }

        if (event.getAuthor().equals(DiscordManager.client.getOurUser())) {
            return;
        }

        if (event.getChannel().equals(event.getAuthor().getOrCreatePMChannel())) {
            if (DiscordCommand.awaitingLinkReply.containsValue(event.getAuthor())) {
                if (event.getMessage().getContent().toLowerCase().startsWith("y")) {
                    DiscordCommand.awaitingLinkReply.forEach((key, value) -> {
                        if (value.equals(event.getAuthor())) {
                            DiscordCommand.awaitingLinkReply.remove(key);
                            Player player = Bukkit.getPlayer(key);
                            CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
                            if (cPlayer != null) {
                                event.getAuthor().getOrCreatePMChannel().sendMessage("Accepted " + player.getName() + "'s sync request. Your Discord and in-game profiles are now synchronized!");
                                cPlayer.getPlayer().sendMessage("§7Your Discord user role (" + DiscordManager.getDiscordRankPrefix(DiscordManager.client.getGuildByID(Settings.discordGuild), event.getAuthor()) + DiscordManager.getMatchingGameRank(DiscordManager.client.getGuildByID(Settings.discordGuild), event.getAuthor(), cPlayer) + "§7) and ID are now synchronized! §c❤");
                                // Set Discord ID
                                cPlayer.setDiscordUserId(event.getAuthor().getLongID());
                                DiscordManager.awardPlayer(cPlayer);
                                cPlayer.setSyncedOnce(true);
                                // Add to ID map.
                                Configuration idmap = DiscordManager.idMap();
                                idmap.set(cPlayer.getDiscordId().toString(), cPlayer.getUniqueId().toString());
                                idmap.save();
                                DiscordManager.syncRank(cPlayer);
                            } else {
                                event.getAuthor().getOrCreatePMChannel().sendMessage("The player requesting the sync is no longer in-game. Cancelling their request.");
                            }
                        }
                    });
                    return;
                }

                if (event.getMessage().getContent().toLowerCase().startsWith("n")) {
                    DiscordCommand.awaitingLinkReply.forEach((key, value) -> {
                        if (value.equals(event.getAuthor())) {
                            DiscordCommand.awaitingLinkReply.remove(key);
                            Player player = Bukkit.getPlayer(key);
                            if (player != null) {
                                player.sendMessage("§7Your Discord sync request was denied by " + value.getName() + ".");
                            }
                            event.getAuthor().getOrCreatePMChannel().sendMessage("You denied the sync request. If you suspect someone is attempting to steal your account information, message a member of DoodCraft staff immediately with the name of the person requesting the sync.");
                        }
                    });
                    return;
                }

                event.getAuthor().getOrCreatePMChannel().sendMessage("I didn't understand that. Please reply with a yes or no.");
                return;
            }

            event.getAuthor().getOrCreatePMChannel().sendMessage("I'm sorry " + event.getAuthor().getName() + ", but it appears that I am not expecting any replies from you at this time. It's possible any previous requests you received from me expired or have been cancelled. :thinking:");
            return;
        }

        if (event.getChannel().getLongID() == Settings.discordChannel) {
            if (event.getMessage().getContent().startsWith(".help")) {
                DiscordManager.sendGameHelp();
                return;
            }

            if (event.getMessage().getContent().startsWith(".who")) {
                DiscordManager.sendGameWho();
                return;
            }

            if (event.getMessage().getContent().startsWith(".sync")) {
                DiscordManager.sendSync(event.getAuthor());
                return;
            }

            if (event.getMessage().getContent().startsWith(".nuke")) {
                DiscordManager.sendNukeRoll(event.getAuthor());
                return;
            }

            StaticMethods.log("[DISCORD] " + event.getAuthor().getName() + ": " + event.getMessage().getContent());
            DiscordManager.broadcastToMinecraft("§d[Discord]§8§r " + DiscordManager.getDiscordRankPrefix(event.getGuild(), event.getAuthor()) + event.getAuthor().getName() + "§8: §7" + event.getMessage());
        }
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        DiscordManager.client.changePlayingText("IP: mc.doodcraft.net");
        DiscordManager.updateTopic();
    }
}