package net.doodcraft.oshcon.bukkit.doodcore.config;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;

import java.io.File;

public class Settings {

    public static String discordToken;
    public static Long discordGuild;
    public static Long discordChannel;

    public static void addConfigDefaults() {
        discordToken = "bot-token";
        discordGuild = 999999999999999999L;
        discordChannel = 999999999999999999L;

        Configuration config = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "config.yml");
        config.add("Discord.Token", discordToken);
        config.add("Discord.Guild", discordGuild);
        config.add("Discord.Channel", discordChannel);
        config.save();

        setNewConfigValues(config);
    }

    public static void setNewConfigValues(Configuration config) {
        discordToken = config.getString("Discord.Token");
        discordGuild = (Long) config.get("Discord.Guild");
        discordChannel = (Long) config.get("Discord.Channel");
    }

    public static void reload() {
        Configuration config = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "config.yml");
        setNewConfigValues(config);
    }
}
