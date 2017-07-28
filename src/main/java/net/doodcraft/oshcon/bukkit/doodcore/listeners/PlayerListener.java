package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import at.pcgamingfreaks.MarriageMaster.Bukkit.Commands.MarryChat;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import com.gmail.nossr50.api.ChatAPI;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkTask;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        CorePlayer.createCorePlayer(player);
        UUID uuid = player.getUniqueId();

        CorePlayer cPlayer = CorePlayer.players.get(uuid);

        CorePlayer.activeTimes.putIfAbsent(cPlayer.getUniqueId(), System.currentTimeMillis());

        // AFK Task
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new AfkTask(event.getPlayer()), 0L, 10L);
        if (!AfkHandler.tasks.containsKey(uuid)) {
            AfkHandler.tasks.put(uuid, task);
        } else {
            Bukkit.getScheduler().cancelTask(AfkHandler.tasks.get(uuid));
            AfkHandler.tasks.remove(uuid);
            AfkHandler.tasks.put(uuid, task);
        }

        if (DiscordManager.toggled) {
            if (!cPlayer.isIgnoringDiscord()) {
                DiscordManager.sendGameLogin(player);
            }
        }

        if (cPlayer.getDiscordUserId() == 0) {
            DiscordManager.addReminderTask(cPlayer);
        }

        // Check total play time, give Veteran rank if over 86400000 millis.
        if (cPlayer.getCurrentActiveTime() >= 86400000L) {
            // They need to be a Veteran now.
            // Update their role on Discord. Let syncRank do the rest.
            // This requires their account to be synced to discord. Check if they are ignoring reminders.
            if (Arrays.toString(Vault.permission.getPlayerGroups(null, cPlayer.getPlayer())).contains("Veteran")) {
                StaticMethods.log(cPlayer.getName() + " is already a Veteran.");
                return;
            }

            if (cPlayer.getDiscordUserId() != 0L) {
                if (DiscordManager.client.getUserByID(cPlayer.getDiscordUserId()) != null) {
                    IUser user = DiscordManager.client.getUserByID(cPlayer.getDiscordUserId());
                    if (!user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Veteran")) {
                        user.addRole(DiscordManager.client.getGuildByID(Settings.discordGuild).getRolesByName("Veterans").get(0));
                    }
                } else {
                    StaticMethods.log(cPlayer.getName() + " was supposed to be ranked to Veteran, however their Discord ID is invalid.");
                }
            } else {
                if (!cPlayer.isIgnoringDiscordReminder()) {
                    cPlayer.getPlayer().sendMessage("§7You've earned the §2Veteran§7 role, however, you must sync your Discord account to get it.");
                    cPlayer.getPlayer().sendMessage("§7Use §b/discord sync §7to learn how to sync your account.");
                    cPlayer.getPlayer().sendMessage("§7If you do not want to see this message anymore, use §b/discord reminder");
                }
            }

            // They were ignoring reminders, so we cannot give them the Veteran rank.
        }

        DiscordManager.syncRank(cPlayer);
        DiscordManager.updateTopic();

        // Extra values we don't really need for the cPlayer object, but should keep jic.
        Configuration data = cPlayer.getDataFile();
        data.set("LastJoined", System.currentTimeMillis());
        data.save();

        event.setJoinMessage(Messages.parse(cPlayer, "§8[§7<time>§8] §7<roleprefix><name> §7joined Bending."));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = event.getPlayer().getUniqueId();
        CorePlayer cPlayer = CorePlayer.players.get(uuid);

        if (cPlayer.isCurrentlyAfk()) {
            cPlayer.setAfkStatus(false, "Quitting");
        }

        cPlayer.setActiveTime(cPlayer.getCurrentActiveTime());
        cPlayer.setAfkTime(cPlayer.getCurrentAfkTime());

        if (CorePlayer.activeTimes.containsKey(cPlayer.getUniqueId())) {
            CorePlayer.activeTimes.remove(cPlayer.getUniqueId());
        }

        if (CorePlayer.afkTimes.containsKey(cPlayer.getUniqueId())) {
            CorePlayer.afkTimes.remove(cPlayer.getUniqueId());
        }

        if (AfkHandler.tasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(AfkHandler.tasks.get(uuid));
            AfkHandler.lastAction.remove(uuid);
            AfkHandler.lastLocation.remove(uuid);
            AfkHandler.tasks.remove(uuid);
        }

        if (DiscordManager.toggled) {
            if (!cPlayer.isIgnoringDiscord()) {
                DiscordManager.sendGameQuit(player);
            }
        }

        DiscordManager.updateTopic();

        event.setQuitMessage(Messages.parse(cPlayer, "§8[§7<time>§8] §7<roleprefix><name> §7quit."));

        // Extra values we don't really need for the cPlayer object, but should keep jic.
        Configuration data = cPlayer.getDataFile();
        data.set("LastLocation", StaticMethods.getLocString(cPlayer.getPlayer().getLocation()));
        data.set("LastQuit", System.currentTimeMillis());
        data.save();

        // FINISH HIM
        CorePlayer.destroy(cPlayer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CorePlayer cPlayer = CorePlayer.players.get(uuid);
        String message = event.getMessage();

        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "");
        }

        event.getRecipients().clear();

        if (Compatibility.isHooked("MarriageMaster")) {
            MarryChat mc = new MarryChat((MarriageMaster) Compatibility.getPlugin("MarriageMaster"));
            if (mc.pcl.contains(player)) {
                return;
            }
        }

        if (Compatibility.isHooked("mcMMO")) {
            if (ChatAPI.isUsingPartyChat(player)) {
                return;
            }
            if (ChatAPI.isUsingAdminChat(player)) {
                return;
            }
        }

        if (Compatibility.isHooked("TownyChat")) {
            // TODO
        }

        if (!event.isCancelled()) {
            if (DiscordManager.toggled) {
                if (DiscordManager.client != null) {
                    // TODO: Allow @mentioning a user from in-game.
                    DiscordManager.sendGameChat(player, StaticMethods.removeColor(message));
                }
            }

            String msg;
            if (player.hasPermission("core.chat.colors")) {
                msg = StaticMethods.addColor(event.getMessage()).replaceAll("§k", "");
            } else {
                msg = StaticMethods.removeColor(event.getMessage());
            }

            DiscordManager.broadcastJson(player, msg);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {

        // I don't EVER want to see "magic" characters anywhere, ever again.
        event.setMessage(event.getMessage().replaceAll("&k", ""));

        if (event.getMessage().split(" ")[0].equalsIgnoreCase("/afk")) {
            return;
        }

        if (event.getMessage().split(" ")[0].equalsIgnoreCase("/mytime")) {
            return;
        }

        CorePlayer cPlayer = CorePlayer.players.get(event.getPlayer().getUniqueId());
        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "Using commands/chatting");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());

        if (cPlayer != null) {
            cPlayer.setAfkStatus(false, "");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (!DiscordManager.toggled) {
            return;
        }

        DiscordManager.sendGameDeath(event.getEntity(), event.getDeathMessage());
    }
}