package net.doodcraft.oshcon.bukkit.doodcore;

import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.commands.*;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.entitymanagement.EntityManagement;
import net.doodcraft.oshcon.bukkit.doodcore.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class DoodCorePlugin extends JavaPlugin {

    public static Plugin plugin;
    public static Random random;

    // TODO: PvP protection
    // TODO: Towny chat integration
    // TODO: Fix EnderPads (bad link id check)
    // TODO: Chisel - add containers

    @Override
    public void onEnable() {
        plugin = this;
        random = new Random();

        setExecutors();
        registerListeners();

        Compatibility.checkHooks();
        Settings.addConfigDefaults();
        EntityManagement.startItemPurgeTask();

        for (Player p : Bukkit.getOnlinePlayers()) {
            CorePlayer.createCorePlayer(p);
        }

        AfkHandler.addAllPlayers();

        try {
            DiscordManager.setupDiscord(Settings.discordToken);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        DiscordManager.sendGameOnline();
    }

    @Override
    public void onDisable() {
        AfkHandler.removeAllPlayers();

        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                DiscordManager.client.getChannelByID(Settings.discordChannel).changeTopic("OFFLINE (mc.doodcraft.net)");
                DiscordManager.sendGameOffline();
                DiscordManager.client.logout();
            }
        }
    }

    public void setExecutors() {
        getCommand("core").setExecutor(new CoreCommand());
        getCommand("discord").setExecutor(new DiscordCommand());
        getCommand("afk").setExecutor(new AfkCommand());
        getCommand("nick").setExecutor(new NickCommand());
        getCommand("testtext").setExecutor(new TestTextCommand());
        getCommand("me").setExecutor(new MeCommand());
        getCommand("say").setExecutor(new SayCommand());
        getCommand("sudo").setExecutor(new SudoCommand());
        getCommand("mytime").setExecutor(new MyTimeCommand());
    }

    public void registerListeners() {
        registerEvents(plugin, new PlayerListener());
    }

    public static void registerEvents(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}