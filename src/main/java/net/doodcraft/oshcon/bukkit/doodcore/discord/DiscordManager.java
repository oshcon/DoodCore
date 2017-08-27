package net.doodcraft.oshcon.bukkit.doodcore.discord;

import mkremins.fanciful.FancyMessage;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.badges.BadgeType;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.DiscordReminderTask;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.DiscordUpdateTask;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
                        DiscordManager.client.getChannelByID(Settings.discordChannel).changeTopic("ONLINE (IP: mc.doodcraft.net): " + Bukkit.getOnlinePlayers().size() + "/32 players online");
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
                        DiscordManager.client.changePlayingText(games.get(game).replaceAll("<players>", String.valueOf(CorePlayer.getPlayers().size())));
                    }
                });
            }
        }
    }

    public static void broadcastToMinecraft(String string) {
        for (CorePlayer cPlayer : CorePlayer.getPlayers().values()) {
            if (!cPlayer.isIgnoringDiscord()) {
                cPlayer.getPlayer().sendMessage(string);
            }
        }
    }

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
                        builder.withDescription("**`[" + StaticMethods.removeColor(cPlayer.getNick()) + "]`**  " + message);
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
                        builder.withDescription(StaticMethods.removeColor("*" + cPlayer.getNick() + " " + message + "*"));
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
                    builder.withDescription("**`[DOODCRAFT]`**  " + message);
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
                        builder.withDescription("**`[JOIN]`**  " + cPlayer.getName() + " joined the game.");
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
                        builder.withDescription("**`[QUIT]`**  " + cPlayer.getName() + " left the game.");
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
                        builder.withDescription("**`[DEATH]`**  " + message);
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

    public static void sendNukeRoll(IUser user) {
        Bukkit.getScheduler().runTaskAsynchronously(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.appendField("**`[NUKE]`**", "Nuke everyone online by carefully following the instructions of [this video](https://youtu.be/dQw4w9WgXcQ).", true);
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
                cPlayer.getPlayer().sendMessage("§7You've earned §6§l$500 §7for syncing your accounts!");
            } catch (Exception ex) {
                cPlayer.getPlayer().sendMessage("§7There was an error giving your sync reward. Notify a staff member.");
                StaticMethods.log("There was an error giving a sync reward ($500) to " + cPlayer.getName());
            }
        } else {
            cPlayer.getPlayer().sendMessage("§7You cannot receive more than one reward for this action. Sorry!");
        }
    }

    public static void syncRank(CorePlayer cPlayer) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group set Member");

        if (cPlayer.getDiscordId() != 0L) {
            IUser user = DiscordManager.client.getUserByID(cPlayer.getDiscordId());
            if (user != null) {
                String rank = DiscordManager.getMatchingGameRank(DiscordManager.client.getGuildByID(Settings.discordGuild), user, cPlayer);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group set " + rank);
                if (user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Supporter")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Supporter");

                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {
                            cPlayer.addBadge(new Badge(BadgeType.SUPPORTER));
                            cPlayer.getPlayer().sendMessage("§6Thank you for supporting DoodCraft! §c❤");
                        }
                    }, 20L);
                } else {
                    cPlayer.removeBadge(new Badge(BadgeType.SUPPORTER));
                }
            }
        }

        autoRankVeteran(cPlayer);
        autoRankSupporter(cPlayer);
    }

    public static void autoRankVeteran(CorePlayer cPlayer) {
        if (cPlayer.getCurrentActiveTime() >= Settings.veteranTime * 1000) {
            // They need to be a Veteran now.
            // Update their role on Discord. Let syncRank do the rest.
            // This requires their account to be synced to discord. Check if they are ignoring reminders.
            if (!Compatibility.isHooked("Vault") || Vault.permission == null || !Vault.permission.isEnabled()) {
                return;
            }

            if (Arrays.toString(Vault.permission.getPlayerGroups(null, cPlayer.getPlayer())).contains("Veteran")) {
                return;
            }

            if (cPlayer.getDiscordId() != 0L) {
                if (DiscordManager.client.getUserByID(cPlayer.getDiscordId()) != null) {
                    IUser user = DiscordManager.client.getUserByID(cPlayer.getDiscordId());

                    // If they have the Veteran rank on Discord, give it in-game.
                    if (user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Veteran")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Veteran");
                    }

                    // If they have the Veteran rank in-game, give it on Discord.
                    if (Vault.permission.getPrimaryGroup(null, cPlayer.getPlayer()).equalsIgnoreCase("Veteran")) {
                        if (!user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Veteran")) {
                            user.addRole(DiscordManager.client.getGuildByID(Settings.discordGuild).getRolesByName("Veterans").get(0));
                        }
                    }
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Veteran");
                    StaticMethods.log(cPlayer.getName() + " ranked to Veteran in-game, however their Discord ID is invalid, skipping Discord promotion.");
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Veteran");
            }
        }
    }

    // Only attempt this after attempting to sync Discord.
    public static void autoRankSupporter(CorePlayer cPlayer) {

        if (!Compatibility.isHooked("Vault") || Vault.permission == null || !Vault.permission.isEnabled()) {
            return;
        }

        if (Arrays.toString(Vault.permission.getPlayerGroups(null, cPlayer.getPlayer())).contains("Supporter")) {
            StaticMethods.log(cPlayer.getName() + " is already a Supporter.");
            return;
        }

        if (cPlayer.getDiscordId() == 0L) {
            try {
                URL url = new URL("https://gist.githubusercontent.com/oshcon/c94e617050ff370987479e77048cda50/raw/ae890a4cd4feadc5f3c617aee667137ff977bff7/supporters.txt");
                File supporterList = new File(url.getFile());
                Configuration supporters = new Configuration(supporterList);
                if (supporters.getKeys(false).contains(cPlayer.getName())) {
                    // Check the time difference. 5259492000 is two months.
                    Long time = (Long) supporters.get(cPlayer.getName());
                    if ((System.currentTimeMillis() - time) < 5259492000L) {
                        // Their supporter status has not expired.
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Supporter");
                        cPlayer.getPlayer().sendMessage("§6Thank you for supporting DoodCraft! §c❤");
                        cPlayer.addBadge(new Badge(BadgeType.SUPPORTER));
                        return;
                    }
                    // Their supporter status has expired. Notify them of this, since they will not get a Discord notification.
                    // TODO: Remove the user from the supporter list. Will need to use the Gist API, if possible.
                    cPlayer.removeBadge(new Badge(BadgeType.SUPPORTER));
                    StaticMethods.log(cPlayer.getName() + "'s supporter status has expired. Remind them to renew?");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void broadcastJson(Player player, String message) {
        // Schedule this as a sync task, since we are potentially using Bukkit.dispatchCommand() or other Bukkit API calls async.
        Bukkit.getScheduler().runTask(DoodCorePlugin.plugin, new Runnable() {
            @Override
            public void run() {
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                FancyMessage msg = new FancyMessage(Messages.parse(cPlayer, "<roleprefix><nick>"));
                String hover = Messages.parse(cPlayer, Messages.getHover(cPlayer));

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

        idmap.remove(cPlayer.getDiscordId().toString());
        idmap.save();

        cPlayer.setDiscordUserId(0L);
    }
}