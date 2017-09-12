package net.doodcraft.oshcon.bukkit.doodcore.discord;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.DiscordReminderTask;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.DiscordUpdateTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DiscordManager {

    public static IDiscordClient client;
    public static Boolean toggled = true;
    public static List<String> games;

    public static void setupDiscord(String token) {
        games = new ArrayList<>();
        games.add("IP: mc.doodcraft.net");
        games.add("<players> online now");
        games.add("Type !help for help");

        ClientBuilder cb = new ClientBuilder();
        cb.withToken(token);

        try {
            client = cb.build();
            client.getDispatcher().registerListener(new DiscordListener());
            login();
        } catch (Exception ex) {
            StaticMethods.log(ex.getLocalizedMessage());
        }
    }

    public static void login() {
        if (client != null) {
            if (!client.isLoggedIn()) {
                client.login();
            } else {
                client.logout();
                client.login();
            }

            new DiscordUpdateTask().runTaskTimerAsynchronously(DoodCorePlugin.plugin, 1L, 600);
        }
    }

    public static void updateTopic() {
        if (client != null) {
            if (client.isLoggedIn()) {
                Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        DiscordManager.client.getChannelByID(Settings.discordChannel).changeTopic("ONLINE (IP: mc.doodcraft.net): " + CorePlayer.getPlayerCount() + "/42 players online");
                    }
                });
            }
        }
    }

    public static void updateGame() {
        if (client != null) {
            if (client.isLoggedIn()) {

                Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
                    @Override
                    public void run() {
                        int game = DoodCorePlugin.random.nextInt(games.size());
                        DiscordManager.client.changePlayingText(games.get(game).replaceAll("<players>", String.valueOf(CorePlayer.getPlayerCount())));
                    }
                });
            }
        }
    }

    public static String getDiscordRankPrefix(IGuild guild, IUser user) {
        List<IRole> roles = guild.getRolesForUser(user);

        if (roles.toString().contains("Bots")) {
            return "§c";
        }
        if (roles.toString().contains("Administrators")) {
            return "§5";
        }
        if (roles.toString().contains("Bouncers")) {
            return "§3";
        }
        if (roles.toString().contains("Artists")) {
            return "§d";
        }
        if (roles.toString().contains("Trainees")) {
            return "§b";
        }
        if (roles.toString().contains("Veterans")) {
            return "§2";
        }

        return "§e";
    }

    public static String getMatchingGameRank(IGuild guild, IUser user, CorePlayer cPlayer) {
        if (guild.getUsers().contains(user)) {
            List<IRole> roles = guild.getRolesForUser(user);

            if (roles.toString().contains("Administrators")) {
                return "Administrator";
            }
            if (roles.toString().contains("Bouncers")) {
                return "Bouncer";
            }
            if (roles.toString().contains("Artists")) {
                return "Artist";
            }
            if (roles.toString().contains("Trainees")) {
                return "Trainee";
            }
            if (roles.toString().contains("Veterans")) {
                return "Veteran";
            }
        } else {
            unSyncDiscord(cPlayer);
        }

        // default
        return "Member";
    }

    public static Configuration idMap() {
        return new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "discord-idmap.yml");
    }

    public static void addReminderTask(CorePlayer player) {
        if (DiscordReminderTask.tasks.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(DiscordReminderTask.tasks.get(player.getUniqueId()));
            DiscordReminderTask.tasks.remove(player.getUniqueId());
            Integer task = startReminderTask(player);
            DiscordReminderTask.tasks.put(player.getUniqueId(), task);
        } else {
            Integer task = startReminderTask(player);
            DiscordReminderTask.tasks.put(player.getUniqueId(), task);
        }
    }

    public static Integer startReminderTask(CorePlayer player) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new DiscordReminderTask(player), 180L * 20L);
        return task.getTaskId();
    }

    public static void awardPlayer(CorePlayer cPlayer) {
        if (!cPlayer.hasSyncedBefore()) {
            try {
                Vault.economy.depositPlayer(cPlayer.getPlayer(), 500);
                cPlayer.getPlayer().sendMessage("§7You've earned §6§l500Ƶ §7for syncing your accounts!");
            } catch (Exception ex) {
                cPlayer.getPlayer().sendMessage("§7There was an error giving your sync reward. Notify a staff member.");
                StaticMethods.log("There was an error giving a sync reward (500Ƶ) to " + cPlayer.getName());
            }
        } else {
            cPlayer.getPlayer().sendMessage("§7You cannot receive more than one reward for this action. Sorry!");
        }
    }

    public static void unSyncDiscord(CorePlayer cPlayer) {
        Configuration idmap = DiscordManager.idMap();

        // There could be orphans?
        for (String id : idmap.getKeys(false)) {
            if (idmap.get(id).equals(cPlayer.getUniqueId().toString())) {
                idmap.remove(id);
            }
        }

        idmap.remove(cPlayer.getDiscordId().toString());
        idmap.save();

        cPlayer.setDiscordUserId(0L);
    }
}