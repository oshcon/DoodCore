package net.doodcraft.oshcon.bukkit.doodcore;

import de.slikey.effectlib.EffectManager;
import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.badges.BadgeListener;
import net.doodcraft.oshcon.bukkit.doodcore.commands.*;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordMessages;
import net.doodcraft.oshcon.bukkit.doodcore.entitymanagement.EntityManagement;
import net.doodcraft.oshcon.bukkit.doodcore.listeners.*;
import net.doodcraft.oshcon.bukkit.doodcore.pvpmanager.PvPLogger;
import net.doodcraft.oshcon.bukkit.doodcore.util.Lag;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;

public class DoodCorePlugin extends JavaPlugin {

    public static Plugin plugin;
    public static Random random;
    public static EffectManager effectManager;

    @Override
    public void onEnable() {
        plugin = this;
        random = new Random();
        effectManager = new EffectManager(plugin);

        Compatibility.checkHooks();

        setExecutors();
        registerListeners();
        cacheAllNames();

        Settings.addConfigDefaults();
        EntityManagement.startItemPurgeTask();
        PlayerMethods.loadAllCorePlayers();
        AfkHandler.addAllPlayers();
        BackCommand.loadDeathLocations();
        PvPLogger.setupBlockedCommands();

        try {
            DiscordManager.setupDiscord(Settings.discordToken);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        DiscordMessages.sendGameOnline();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Lag(), 100L, 1L);
    }

    @Override
    public void onDisable() {
        AfkHandler.removeAllPlayers();
        BackCommand.dumpDeathLocations();

        PlayerMethods.dumpAllCorePlayers();

        if (DiscordManager.client != null) {
            if (DiscordManager.client.isLoggedIn()) {
                DiscordManager.client.getChannelByID(Settings.discordChannel).changeTopic("OFFLINE (mc.doodcraft.net)");
                DiscordMessages.sendGameOffline();
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
        getCommand("wild").setExecutor(new WildCommand());
        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("seen").setExecutor(new SeenCommand());
        getCommand("givepet").setExecutor(new GivePetCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("back").setExecutor(new BackCommand());
        getCommand("homes").setExecutor(new HomesCommand());
        getCommand("removehome").setExecutor(new RemoveHomeCommand());
        getCommand("vote").setExecutor(new VoteCommand());
        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("tpahere").setExecutor(new TpahereCommand());
        getCommand("tpcancel").setExecutor(new TpcancelCommand());
        getCommand("tpaccept").setExecutor(new TpacceptCommand());
        getCommand("tpdeny").setExecutor(new TpdenyCommand());
        getCommand("track").setExecutor(new TrackCommand());
        getCommand("badges").setExecutor(new BadgesCommand());
        getCommand("pvp").setExecutor(new PvPCommand());
        getCommand("head").setExecutor(new HeadCommand());
    }

    public void registerListeners() {
        registerEvents(plugin, new PlayerListener());
        registerEvents(plugin, new GivePetCommand());
        registerEvents(plugin, new PvPLogger());
        registerEvents(plugin, new BadgeListener());
        registerEvents(plugin, new ChatListener());
        registerEvents(plugin, new EntityListener());
        registerEvents(plugin, new CoreSignListener());

        if (Compatibility.isHooked("Votifier")) {
            registerEvents(plugin, new VotifierListener());
        }
    }

    public static void registerEvents(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public static void cacheAllNames() {
        File directory = new File(plugin.getDataFolder() + File.separator + "data");
        File[] files = directory.listFiles();

        if (files != null) {
            StaticMethods.log("Caching " + files.length + " player names/nicks.");
            for (File f : files) {
                Configuration cData = new Configuration(plugin.getDataFolder() + File.separator + "data" + File.separator + f.getName());
                if (cData.get("Name") != null) {
                    CorePlayer.names.put(cData.getString("Name"), cData.getString("Nick"));
                }
            }
        }
    }
}