package net.doodcraft.oshcon.bukkit.doodcore.config;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
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
                    cPlayer.getPlayer().sendMessage(parse(cPlayer, line));
                }
            } else {
                cPlayer.getPlayer().sendMessage("§cThere was an error getting message: §b" + message);
                StaticMethods.log(message + " was null for " + cPlayer.getName());
            }
        }
    }

    public static String parse(CorePlayer cPlayer, String line) {
        // NAME
        line = line.replaceAll("<uuid>", cPlayer.getUniqueId().toString());
        line = line.replaceAll("<name>", cPlayer.getName());
        line = line.replaceAll("<nick>", cPlayer.getNickName());

        // TIME
        line = line.replaceAll("<time>", StaticMethods.getTimeStamp());
        line = line.replaceAll("<totalactive>", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(cPlayer.getCurrentActiveTime())));
        line = line.replaceAll("<totalafk>", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(cPlayer.getCurrentAfkTime())));

        // VAULT
        if (Compatibility.isHooked("Vault")) {
            line = line.replaceAll("<role>", Vault.chat.getPrimaryGroup(null, cPlayer.getPlayer()));
            line = line.replaceAll("<roleprefix>", Vault.chat.getPlayerPrefix(null, cPlayer.getPlayer()));
        } else {
            line = nullVault(line);
        }

        // TOWNY
        if (Compatibility.isHooked("Towny")) {
            // TODO
        } else {
            line = nullTowny(line);
        }

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
                line = line.replaceAll("<discordid>", cPlayer.getDiscordUserId().toString());
                if (cPlayer.getDiscordUserId() != 0) {
                    try {
                        line = line.replaceAll("<discordname>", DiscordManager.client.getUserByID(cPlayer.getDiscordUserId()).getName());
                        line = line.replaceAll("<discordnick>", DiscordManager.client.getUserByID(cPlayer.getDiscordUserId()).getDisplayName(DiscordManager.client.getGuildByID(Settings.discordGuild)));
                        line = line.replaceAll("<discorddiscriminator>", DiscordManager.client.getUserByID(cPlayer.getDiscordUserId()).getDiscriminator());
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

    public static String nullVault(String line) {
        line = line.replaceAll("<role>", "N/A");
        line = line.replaceAll("<roleprefix>", "N/A");
        return line;
    }

    public static String nullTowny(String line) {
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