package net.doodcraft.oshcon.bukkit.doodcore.discord;

import mkremins.fanciful.FancyMessage;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiscordManager {

    public static IDiscordClient client;
    public static Boolean toggled = true;
    public static List<String> games;

    public static void setupDiscord(String token) {
        games = new ArrayList<>();
        games.add("IP: mc.doodcraft.net");
        games.add("<players> online now");
        games.add("Type .help for help");

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

            Bukkit.getScheduler().scheduleAsyncRepeatingTask(DoodCorePlugin.plugin, new DiscordTask(), 0L, 30*20);
        }
    }

    public static void updateTopic() {
        if (client != null) {
            if (client.isLoggedIn()) {
                DiscordManager.client.getChannelByID(Settings.discordChannel).changeTopic("ONLINE (IP: mc.doodcraft.net): " + Bukkit.getOnlinePlayers().size() + "/32 players online");
            }
        }
    }

    public static void updateGame() {
        if (client != null) {
            if (client.isLoggedIn()) {
                int game = DoodCorePlugin.random.nextInt(games.size());
                ConcurrentLinkedQueue<Player> players = new ConcurrentLinkedQueue<>(Bukkit.getOnlinePlayers());
                DiscordManager.client.changePlayingText(games.get(game).replaceAll("<players>", String.valueOf(players.size())));
            }
        }
    }

    public static void broadcastToMinecraft(String string) {
        for (CorePlayer cPlayer : CorePlayer.players.values()) {
            if (!cPlayer.isIgnoringDiscord()) {
                //
            }
        }
    }

    public static void sendGameChat(Player player, String message) {

        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[" + StaticMethods.removeColor(cPlayer.getNickName()) + "]`**  " + message);
                builder.withAuthorName(StaticMethods.removeColor(player.getName()));
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(66, 179, 244);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } catch (Exception ignored) {}
        }
    }

    public static void sendGameMe(Player player, String message) {
        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[EMOTE]`**  *" + message + "*");
                builder.withAuthorName(StaticMethods.removeColor(player.getName()));
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(66, 179, 244);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } catch (Exception ignored) {}
        }
    }

    public static void sendGameSay(Player player, String message) {
        try {
            if (player != null) {
                CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
                if (cPlayer != null) {
                    if (cPlayer.isIgnoringDiscord()) {
                        return;
                    }
                }
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[DOODCRAFT]`**  " + message);
                builder.withAuthorName(StaticMethods.removeColor(player.getName()));
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(182,66,244);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } else {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[DOODCRAFT]`**  " + message);
                builder.withAuthorName(StaticMethods.removeColor("CONSOLE"));
                builder.withAuthorIcon("https://crafatar.com/avatars/CONSOLE?default=CONSOLE&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(182,66,244);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            }
        } catch (Exception ignored) {}
    }

    public static void sendGameLogin(Player player) {

        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[JOIN]`**  " + player.getName() + " joined the game.");
                builder.withAuthorName(player.getName());
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(72, 244, 66);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } catch (Exception ignored) {}
        }
    }

    public static void sendGameQuit(Player player) {

        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[QUIT]`**  " + player.getName() + " left the game.");
                builder.withAuthorName(player.getName());
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(244, 75, 66);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } catch (Exception ignored) {}
        }
    }

    public static void sendGameDeath(Player player, String message) {

        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
        if (cPlayer != null) {
            if (cPlayer.isIgnoringDiscord()) {
                return;
            }

            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.withDescription("**`[DEATH]`**  " + message);
                builder.withAuthorName(player.getName());
                builder.withAuthorIcon("https://crafatar.com/avatars/" + player.getUniqueId() + "?default=MHF_Steve&overlay");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(229, 244, 66);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
            } catch (Exception ignored) {}
        }
    }

    public static void sendGameWho() {
        try {
            if (Bukkit.getOnlinePlayers().size() <= 0) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
                builder.appendField("**`[WHO]`**", "It doesn't look like anybody is online right now. :thinking:", false);
                builder.withAuthorName("Minecraft");
                builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                builder.withTimestamp(System.currentTimeMillis());
                builder.withColor(182,66,244);
                RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                return;
            }

            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                CorePlayer cPlayer = CorePlayer.players.get(p.getUniqueId());

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

            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
            builder.appendField("**`[WHO]`**", "**`Online (" + names.size() + "):`**  " + StringUtils.join(names, ", "), false);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182,66,244);
            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
        } catch (Exception ignored) {}
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
                            builder.appendField("**`[HELP]`**", "Type .help to see a list of commands.", false);
                            builder.withAuthorName("Minecraft");
                            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
                            builder.withTimestamp(System.currentTimeMillis());
                            builder.withColor(182,66,244);
                            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
                        }
                    }
                }
            },20L);
        } catch (Exception ignored) {}
    }

    public static void sendGameOffline() {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("**`[SERVER STATUS]`**", "The server is now offline. We'll be back momentarily.", true);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182,66,244);
            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
        } catch (Exception ignored) {}
    }

    public static void sendGameHelp() {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.withTitle("**`[HELP]`**");
            builder.appendField("**`[IP]`**", "mc.doodcraft.net", true);
            builder.appendField("**`[COMMANDS]`**", "**`.help`**:  See this list of commands.\n**`.who`**:  View a list of who is on the server.\n**`.sync`**:  Get instructions on how to sync your MC and Discord accounts.\n**`.nuke`**:  Get instructions on how to send a Towny nuke.", true);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182,66,244);
            RequestBuffer.request(() -> DiscordManager.client.getChannelByID(Settings.discordChannel).sendMessage(builder.build()));
        } catch (Exception ignored) {}
    }

    public static void sendSync(IUser user) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("**`[SYNC]`**", "Syncing will pair your Discord account with your in-game profile. Upon logging in to mc.doodcraft.net, your in-game rank will automatically update to match your Discord rank. Get started by joining the server and typing `/discord sync`.", true);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182,66,244);
            RequestBuffer.request(() -> DiscordManager.client.getOrCreatePMChannel(user).sendMessage(builder.build()));
        } catch (Exception ignored) {}
    }

    public static void sendNukeRoll(IUser user) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.appendField("**`[NUKE]`**", "Nuke everyone online by carefully following the instructions of [this video](https://youtu.be/dQw4w9WgXcQ).", true);
            builder.withAuthorName("Minecraft");
            builder.withAuthorIcon("https://hydra-media.cursecdn.com/minecraft.gamepedia.com/c/c5/Grass.png");
            builder.withTimestamp(System.currentTimeMillis());
            builder.withColor(182,66,244);
            RequestBuffer.request(() -> DiscordManager.client.getOrCreatePMChannel(user).sendMessage(builder.build()));
        } catch (Exception ignored) {}
    }

    public static Boolean isStaff(IGuild guild, IUser user) {
        List<IRole> roles = guild.getRolesForUser(user);
        return roles.toString().contains("90256719102083072");
    }

    public static Boolean isStreamer(IGuild guild, IUser user) {
        List<IRole> roles = guild.getRolesForUser(user);
        return roles.toString().contains("Streamer");
    }

    public static Boolean showPresence(IGuild guild, IUser user) {
        if (isStreamer(guild, user)) {
            return true;
        }
        return isStaff(guild, user);
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
        if (roles.toString().contains("Trainees")) {
            return "§b";
        }
        if (roles.toString().contains("Artists")) {
            return "§d";
        }
        if (roles.toString().contains("Veterans")) {
            return "§2";
        }

        return "§a";
    }

    public static String getMatchingGameRank(IGuild guild, IUser user) {
        List<IRole> roles = guild.getRolesForUser(user);

        if (roles.toString().contains("Administrators")) {
            return "Administrator";
        }
        if (roles.toString().contains("Bouncers")) {
            return "Bouncer";
        }
        if (roles.toString().contains("Trainees")) {
            return "Trainee";
        }
        if (roles.toString().contains("Artists")) {
            return "Artist";
        }
        if (roles.toString().contains("Veterans")) {
            return "Veteran";
        }

        // default
        return "Member";
    }

    public static Configuration idMap() {
        return new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "discord-idmap.yml");
    }

    public static void addReminderTask(CorePlayer player) {
        if (ReminderTask.tasks.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(ReminderTask.tasks.get(player.getUniqueId()));
            Integer remove = ReminderTask.tasks.get(player.getUniqueId());
            ReminderTask.tasks.remove(remove);
            Integer task = startReminderTask(player);
            ReminderTask.tasks.put(player.getUniqueId(), task);
        } else {
            Integer task = startReminderTask(player);
            ReminderTask.tasks.put(player.getUniqueId(), task);
        }
    }

    public static Integer startReminderTask(CorePlayer player) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new ReminderTask(player), 180L*20L);
        return task.getTaskId();
    }

    public static void awardPlayer(CorePlayer cPlayer) {
        if (!cPlayer.hasSyncedBefore()) {
            try {
                Vault.economy.depositPlayer(cPlayer.getPlayer(), 2500);
                cPlayer.getPlayer().sendMessage("");
                cPlayer.getPlayer().sendMessage("§7You've earned $2500 for syncing your account!");
            } catch(Exception ex) {
                cPlayer.getPlayer().sendMessage("§7There was an error giving your sync reward. Notify a staff member.");
                StaticMethods.log("There was an error giving a sync reward ($2500) to " + cPlayer.getName());
            }
        } else {
            cPlayer.getPlayer().sendMessage("");
            cPlayer.getPlayer().sendMessage("§7You cannot receive more than one reward for this action. Sorry!");
        }
    }

    public static void syncRank(CorePlayer cPlayer) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group set Member");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                if (cPlayer != null) {
                    IUser user = DiscordManager.client.getUserByID(cPlayer.getDiscordUserId());
                    if (user != null) {
                        String rank = DiscordManager.getMatchingGameRank(DiscordManager.client.getGuildByID(Settings.discordGuild), user);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group set " + rank);
                        if (user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Supporter")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Supporter");
                        }
                    }
                }
            }
        });
    }

    public static void broadcastJson(Player player, String message) {
        // Schedule this as a sync task, since we are potentially using Bukkit.dispatchCommand() or other Bukkit API calls async.
        Bukkit.getScheduler().runTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
                String supporter = "";

                if (Compatibility.isHooked("Vault")) {
                    for (String group : Vault.permission.getPlayerGroups(null, cPlayer.getPlayer())) {
                        if (group.equalsIgnoreCase("Supporter")) {
                            supporter = "§6§lSUPPORTER";
                        }
                    }
                }

                FancyMessage msg = new FancyMessage(Messages.parse(cPlayer, "<roleprefix><nick>"));
                String hover = Messages.parse(cPlayer, "§7Time: §r<time>\n§7Name: §f<name>\n§7Group: §f<roleprefix><role>\n§7Element: §f<element>\n§7Discord: §f<discordname>\n§7Minutes Played: §f§o<totalactive>\n§7Minutes AFK: §f§o<totalafk>");
                if (!supporter.equals("")) {
                    hover = hover + "\n" + supporter;
                }

                msg.tooltip(hover);
                msg.then("§8: §r" + message);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    msg.send(p);
                }
            }
        });
    }

    public static void unSyncDiscord(CorePlayer cPlayer) {
        Configuration idmap = DiscordManager.idMap();

        // There could be orphans?
        for (String id : idmap.getKeys(false)) {
            if (idmap.get(id).equals(cPlayer.getUniqueId().toString())) {
                idmap.remove(id);
            }
        }

        idmap.remove(cPlayer.getDiscordUserId().toString());
        idmap.save();

        cPlayer.setDiscordUserId(0L);
        cPlayer.save();
    }
}