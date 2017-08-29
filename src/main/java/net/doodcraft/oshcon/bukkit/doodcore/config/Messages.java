package net.doodcraft.oshcon.bukkit.doodcore.config;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Messages {

    public static Configuration getMessages() {
        return new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "messages.yml");
    }

    public static void sendMultiLine(CorePlayer cPlayer, String message) {
        if (cPlayer != null) {
            if (getMessages().get(message) != null) {
                for (String line : getMessages().getStringList(message)) {
                    cPlayer.getPlayer().sendMessage(StaticMethods.addColor(parse(cPlayer, line)));
                }
            } else {
                cPlayer.getPlayer().sendMessage("§cThere was an error getting message: §b" + message);
                StaticMethods.log(message + " was null for " + cPlayer.getName());
            }
        }
    }

    public static String getHover(CorePlayer cPlayer) {
        StringBuilder line = new StringBuilder("§7Time: §7<time>\n§7Name: §7<name>\n§7Role: §7<roleprefix><role>");

        if (Compatibility.isHooked("ProjectKorra")) {
            try {
                List<Element> elements = BendingPlayer.getBendingPlayer(cPlayer.getPlayer()).getElements();
                if (elements.size() >= 1) {
                    line.append("\n§7Element: §7<element>");
                }
            } catch (Exception ignored) {
            }
        }

        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                if (cPlayer.getDiscordId() != 0L) {
                    if (DiscordManager.client.getUserByID(cPlayer.getDiscordId()) != null) {
                        line.append("\n§7Discord: §7<roleprefix>@<discordname>#<discorddiscriminator>");
                    }
                }
            }
        }

        int pKills = cPlayer.getPlayerKills().size();
        if (pKills > 0) {
            line.append("\n§7Players Killed: §c").append(pKills);
        }

        if (cPlayer.getBadges().size() > 0) {
            line.append("\n§7Badges:");
            for (Badge b : cPlayer.getBadges()) {
                line.append("\n§7  - ").append(b.getFriendlyName());
            }
        }

        return line.toString();
    }

    public static String parse(CorePlayer cPlayer, String line) {
        // NAME
        line = line.replaceAll("<uuid>", cPlayer.getUniqueId().toString());
        line = line.replaceAll("<name>", cPlayer.getName());
        line = line.replaceAll("<nick>", cPlayer.getNick());

        // TIME
        line = line.replaceAll("<time>", StaticMethods.getSimpleTimeStamp());
        line = line.replaceAll("<totalactive>", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(cPlayer.getCurrentActiveTime())));
        line = line.replaceAll("<totalafk>", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(cPlayer.getCurrentAfkTime())));

        // VAULT
        line = line.replaceAll("<role>", PlayerMethods.getPrimaryGroup(cPlayer.getPlayer()));
        line = line.replaceAll("<roleprefix>", PlayerMethods.getPlayerPrefix(cPlayer.getPlayer()));
        line = line.replaceAll("<chatprefix>", PlayerMethods.getChatPrefix(cPlayer.getPlayer()));

        // PROJECTKORRA
        if (Compatibility.isHooked("ProjectKorra")) {
            try {
                List<Element> elements = BendingPlayer.getBendingPlayer(cPlayer.getPlayer()).getElements();
                if (elements.size() > 1) {
                    line = line.replaceAll("<element>", "§5Avatar§r");
                } else if (elements.size() == 1) {
                    line = line.replaceAll("<element>", elements.get(0).getColor() + elements.get(0).getName() + "§r");
                } else {
                    line = nullProjectKorra(line);
                }
            } catch (Exception ignored) {
                line = nullProjectKorra(line);
            }
        } else {
            line = nullProjectKorra(line);
        }

        // DISCORD
        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                line = line.replaceAll("<discordid>", cPlayer.getDiscordId().toString());
                if (cPlayer.getDiscordId() != 0L) {
                    try {
                        line = line.replaceAll("<discordname>", DiscordManager.client.getUserByID(cPlayer.getDiscordId()).getName());
                        line = line.replaceAll("<discordnick>", DiscordManager.client.getUserByID(cPlayer.getDiscordId()).getDisplayName(DiscordManager.client.getGuildByID(Settings.discordGuild)));
                        line = line.replaceAll("<discorddiscriminator>", DiscordManager.client.getUserByID(cPlayer.getDiscordId()).getDiscriminator());
                    } catch (Exception ex) {
                        StaticMethods.log("Error parsing Discord information for cPlayer: " + ex.getLocalizedMessage());
                        line = line.replaceAll("<discordname>", "N/A");
                        line = line.replaceAll("<discordnick>", "N/A");
                        line = line.replaceAll("<discorddiscriminator>", "");
                    }
                } else {
                    line = nullDiscord(line);
                }
            } else {
                line = nullDiscord(line);
            }
        } else {
            line = nullDiscord(line);
        }

        return line;
    }

    public static String nullProjectKorra(String line) {
        line = line.replaceAll("<element>", "N/A");
        return line;
    }

    public static String nullDiscord(String line) {
        line = line.replaceAll("<discordid>", "N/A");
        line = line.replaceAll("<discordname>", "N/A");
        line = line.replaceAll("<discordnick>", "N/A");
        line = line.replaceAll("<discorddiscriminator>", "N/A");
        return line;
    }
}