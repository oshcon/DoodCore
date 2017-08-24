package net.doodcraft.oshcon.bukkit.doodcore.config;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;

import java.io.File;

public class Settings {

    public static String discordToken;
    public static Long discordGuild;
    public static Long discordChannel;
    public static Boolean purgeItems;
    public static Integer wildRadius;
    public static Long pvpProtection = 21600L;
    public static Long veteranTime = 43200L;

    public static void addConfigDefaults() {
        discordToken = "bot-token";
        discordGuild = 999999999999999999L;
        discordChannel = 999999999999999999L;
        purgeItems = true;
        wildRadius = 5000;

        Configuration config = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "config.yml");
        config.add("Discord.Token", discordToken);
        config.add("Discord.Guild", discordGuild);
        config.add("Discord.Channel", discordChannel);
        config.add("ClearLag.PurgeItems", purgeItems);
        config.add("WildCommand.Radius", wildRadius);
        config.save();

        setNewConfigValues(config);
    }

    public static void setNewConfigValues(Configuration config) {
        discordToken = config.getString("Discord.Token");
        discordGuild = (Long) config.get("Discord.Guild");
        discordChannel = (Long) config.get("Discord.Channel");
        purgeItems = config.getBoolean("ClearLag.PurgeItems");
        wildRadius = config.getInteger("WildCommand.Radius");
    }

    public static void reload() {
        Configuration config = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "config.yml");
        setNewConfigValues(config);
    }
}