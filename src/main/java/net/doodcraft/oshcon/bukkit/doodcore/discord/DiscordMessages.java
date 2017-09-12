package net.doodcraft.oshcon.bukkit.doodcore.discord;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.List;

public class DiscordMessages {

    public static void sendGameChat(Player player, String message) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":speech_balloon:   `" + StaticMethods.removeColor(cPlayer.getNick()) + "`: " + message);
                        builder.withAuthorName(StaticMethods.removeColor(cPlayer.getName()));
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(66, 179, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    public static void sendGameMe(Player player, String message) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(StaticMethods.removeColor(":thought_bubble:   *" + cPlayer.getNick() + " " + message + "*"));
                        builder.withAuthorName(StaticMethods.removeColor(cPlayer.getName()));
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(66, 179, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    public static void sendGameSay(Player player, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.withDescription(":loudspeaker:   " + message);
                    builder.withAuthorName(StaticMethods.removeColor("CONSOLE"));
                    builder.withAuthorIcon("https://crafatar.com/avatars/CONSOLE?default=CONSOLE&overlay");
                    builder.withTimestamp(System.currentTimeMillis());
                    builder.withColor(182, 66, 244);
                    RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                } catch (Exception ignored) {}
            }
        });
    }

    public static void sendGameLogin(Player player) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":white_check_mark:   " + cPlayer.getName() + " joined the game.");
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(72, 244, 66);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    public static void sendGameQuit(Player player) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":x:   " + cPlayer.getName() + " left the game.");
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(244, 75, 66);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    public static void sendGameDeath(Player player, String message) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":skull:   " + StaticMethods.removeColor(message));
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(229, 244, 66);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    public static void sendGameWho() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            CorePlayer cPlayer = CorePlayer.getPlayers().get(p.getUniqueId());

            String name = p.getName();
            if (cPlayer != null) {
                if (cPlayer.isCurrentlyAfk()) {
                    name = "[AFK] " + name;
                }
                if (cPlayer.isIgnoringDiscord()) {
                    name = "[Ignoring Discord] " + name;
                }
            }

            names.add(name);
        }

        Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    if (Bukkit.getOnlinePlayers().size() <= 0) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
                        builder.appendField("**`[WHO]`**", "It doesn't look like anybody is online right now. :thinking:", false);
                        builder.withAuthorName("Minecraft");
                        builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(182, 66, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                        return;
                    }

                    EmbedBuilder builder = new EmbedBuilder();
                    builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
                    builder.appendField("**`[WHO]`**", "**`Online (" + names.size() + "):`**  " + StringUtils.join(names, ", "), false);
                    builder.withAuthorName("Minecraft");
                    builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                    builder.withTimestamp(System.currentTimeMillis());
                    builder.withColor(182, 66, 244);
                    RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    public static void sendGameOnline() {
        try {
            Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    if (DiscordManager.client != null) {
                        if (DiscordManager.client.isLoggedIn()) {
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.appendField("**`[SERVER STATUS]`**", "The server is now online!", true);
                            builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
                            builder.appendField("**`[HELP]`**", "Type !help to see a list of commands.", false);
                            builder.withAuthorName("Minecraft");
                            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                            builder.withTimestamp(System.currentTimeMillis());
                            builder.withColor(182, 66, 244);
                            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                        }
                    }
                }
            }, 40L);
        } catch (Exception ignored) {
        }
    }

    public static void sendGameOffline() {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("**`[SERVER STATUS]`**", "The server is now offline. We'll be back momentarily.", true);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182, 66, 244);
            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
        } catch (Exception ignored) {
        }
    }

    public static void sendGameHelp() {
        Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.withTitle("**`[HELP]`**");
                    builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
                    builder.appendField("**`[COMMANDS]`**", "**`!help`**:  See this list of commands.\n**`!who`**:  View a list of who is on the server.\n**`!sync`**:  Get instructions on how to sync your MC and Discord accounts.\n**`!nuke`**:  Get instructions on how to send a nuke.", true);
                    builder.withAuthorName("Minecraft");
                    builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                    builder.withTimestamp(System.currentTimeMillis());
                    builder.withColor(182, 66, 244);
                    RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    public static void sendSync(IUser user) {
        Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.appendField("**`[SYNC]`**", "Syncing will pair your Discord account with your in-game profile. Upon logging in to the Minecraft server, your in-game rank will automatically update to match your Discord rank. Get started by joining then typing `/discord sync`.", true);
                    builder.withAuthorName("Minecraft");
                    builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                    builder.withTimestamp(System.currentTimeMillis());
                    builder.withColor(182, 66, 244);
                    RequestBuffer.request(() -> DiscordManager.client.getOrCreatePMChannel(user).sendMessage(builder.build()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    public static void sendBadgeAward(Player player, Badge badge) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":military_medal:   " + cPlayer.getName() + " earned a special badge!");
                        builder.appendField(StaticMethods.removeColor(badge.getFriendlyName()), badge.getDescription(), true);
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(66, 179, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    public static void sendAfkMessage(Player player, String message) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":toilet:   " + cPlayer.getName() + " is now afk. **`" + StaticMethods.removeColor(message) + "`**");
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(66, 179, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    public static void sendUnAfkMessage(Player player) {
        CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.withDescription(":toilet:   " + cPlayer.getName() + " is no longer afk.");
                        builder.withAuthorName(cPlayer.getName());
                        builder.withAuthorIcon("https://crafatar.com/avatars/" + cPlayer.getName() + "?default=MHF_Steve&overlay");
                        builder.withTimestamp(System.currentTimeMillis());
                        builder.withColor(66, 179, 244);
                        RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }
}
