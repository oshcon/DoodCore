package net.doodcraft.oshcon.bukkit.doodcore.coreplayer;

import com.google.common.base.Joiner;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.AfkCheckTask;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.WarmupTeleportTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CorePlayer {

    public static Map<UUID, Long> activeTimes = new ConcurrentHashMap<>();
    public static Map<UUID, Long> afkTimes = new ConcurrentHashMap<>();
    private static Map<UUID, CorePlayer> players = new ConcurrentHashMap<>();

    public static Map<UUID, CorePlayer> getPlayers() {
        return players;
    }

    public static void addPlayer(CorePlayer cPlayer) {
        getPlayers().put(cPlayer.getUniqueId(), cPlayer);
    }

    public static void removePlayer(CorePlayer cPlayer) {
        getPlayers().remove(cPlayer.getUniqueId());
    }

    public static CorePlayer createCorePlayer(Player player) {
        CorePlayer cPlayer = new CorePlayer(player.getUniqueId());

        // todo: load data
        cPlayer.loadData();

        // Alerts
        if (DiscordManager.toggled) {
            if (!cPlayer.isIgnoringDiscord()) {
                DiscordManager.sendGameLogin(player);
            }
        }

        // Create Tasks
        UUID uuid = cPlayer.getUniqueId();

        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(DoodCorePlugin.plugin, new AfkCheckTask(cPlayer.getPlayer()), 0L, 10L);
        if (!AfkHandler.tasks.containsKey(uuid)) {
            AfkHandler.tasks.put(uuid, task);
        } else {
            Bukkit.getScheduler().cancelTask(AfkHandler.tasks.get(uuid));
            AfkHandler.tasks.remove(uuid);
            AfkHandler.tasks.put(uuid, task);
        }

        if (cPlayer.getDiscordId() == 0L) {
            DiscordManager.addReminderTask(cPlayer);
        }

        // Other Stuff
        CorePlayer.activeTimes.putIfAbsent(cPlayer.getUniqueId(), System.currentTimeMillis());
        DiscordManager.syncRank(cPlayer);
        DiscordManager.updateTopic();

        cPlayer.setLastJoined(System.currentTimeMillis());

        cPlayer.reload();

        Bukkit.broadcastMessage(Messages.parse(cPlayer, "§8[§7<time>§8] §7<roleprefix><name> §7joined Bending."));

        new CorePlayerCreateEvent(cPlayer);
        return cPlayer;
    }

    private UUID uuid;
    private Configuration data;
    private String name;
    private String nick;
    private Boolean afkStatus;
    private Long activeTime;
    private Long afkTime;
    private Map<String, String> homes;
    private Long discordId;
    private Boolean syncedOnce;
    private Boolean ignoresDiscord;
    private Boolean ignoresDiscordReminders;
    private Long lastJoined;
    private Long lastQuit;
    private String lastLocation;
    private int votes;
    private Long lastVote;
    private Boolean thankedForOfflineVote;

    public CorePlayer(UUID uuid) {
        // Default values. These can/will be updated after creating the initial CorePlayer object.
        this.uuid = uuid;
        this.data = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + uuid + ".yml");
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.nick = Bukkit.getOfflinePlayer(uuid).getName();
        this.afkStatus = false;
        this.activeTime = 0L;
        this.afkTime = 0L;
        this.homes = new ConcurrentHashMap<>();
        this.discordId = 0L;
        this.syncedOnce = false;
        this.ignoresDiscord = false;
        this.ignoresDiscordReminders = false;
        this.lastJoined = System.currentTimeMillis();
        this.lastQuit = 0L;
        if (Bukkit.getPlayer(uuid) != null) {
            this.lastLocation = StaticMethods.getRoundedLocString(Bukkit.getPlayer(uuid).getLocation());
        } else {
            this.lastLocation = "null";
        }
        this.votes = 0;
        this.lastVote = 0L;
        this.thankedForOfflineVote = true;

        // Add CorePlayer to Map
        reload();
    }

    // UUID
    public UUID getUniqueId() {
        return this.uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
        reload();
    }

    // DATA
    public Configuration getData() {
        return this.data;
    }

    public void saveData() {
        reload();

        Configuration data = getData();

        data.set("UUID", getUniqueId().toString());
        data.set("Name", getName());
        data.set("Nick", getNick());
        data.set("Time.ActiveTime", getCurrentActiveTime());
        data.set("Time.AfkTime", getCurrentAfkTime());
        data.set("Discord.Id", getDiscordId());
        data.set("Discord.SyncedOnce", hasSyncedBefore());
        data.set("Discord.Ignoring", isIgnoringDiscord());
        data.set("Discord.IgnoringReminders", isIgnoringDiscordReminder());
        data.set("LastJoined", getLastJoined());
        data.set("LastQuit", getLastQuit());
        data.set("LastLocation", getLastLocation());
        data.set("Voting.Total", getTotalVotes());
        data.set("Voting.LastVote", getLastVote());
        data.set("Voting.Thanked", getThankedForOfflineVote());

        if (data.get("Homes") != null) {
            data.remove("Homes");
        }
        data.getYaml().createSection("Homes", getHomes());

        data.save();
    }

    // NAME
    public String getName() {
        return this.name;
    }

    // NICK
    public String getNick() {
        return this.nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
        reload();
    }

    // AFK STATUS
    public void setAfkStatus(Boolean status, String reason) {
        if (status) {
            if (!this.afkStatus) {
                this.afkStatus = true;
                afkTimes.putIfAbsent(this.uuid, System.currentTimeMillis());
                setActiveTime(getCurrentActiveTime());
                activeTimes.remove(this.uuid);
                if (reason == null) {
                    Bukkit.broadcastMessage(PlayerMethods.getPlayerPrefix(getPlayer()) + getPlayer().getDisplayName() + " §7is now AFK.");
                } else {
                    Bukkit.broadcastMessage(PlayerMethods.getPlayerPrefix(getPlayer()) + getPlayer().getDisplayName() + " §7is now AFK. [§3" + reason + "§7]");
                }
            }
        } else {
            if (this.afkStatus) {
                this.afkStatus = false;
                setAfkTime(getCurrentAfkTime());
                afkTimes.remove(this.uuid);
                CorePlayer.activeTimes.putIfAbsent(this.uuid, System.currentTimeMillis());
                Bukkit.broadcastMessage(PlayerMethods.getPlayerPrefix(getPlayer()) + getPlayer().getDisplayName() + " §7is no longer AFK.");
            }
            AfkHandler.lastAction.put(this.uuid, System.currentTimeMillis());
        }

        reload();
    }

    public Boolean isCurrentlyAfk() {
        return this.afkStatus;
    }

    // TIME ACTIVE
    public Long getActiveTime() {
        return this.activeTime;
    }

    public Long getCurrentActiveTime() {
        if (CorePlayer.activeTimes.containsKey(this.uuid)) {
            Long add = System.currentTimeMillis() - CorePlayer.activeTimes.get(this.uuid);
            CorePlayer.activeTimes.put(this.uuid, System.currentTimeMillis());
            this.addActiveTime(add);
            return this.getActiveTime();
        } else {
            return this.getActiveTime();
        }
    }

    public void addActiveTime(Long add) {
        this.setActiveTime(this.getActiveTime() + add);
        reload();
    }

    public void setActiveTime(Long time) {
        this.activeTime = time;
        reload();
    }

    // TIME AFK
    public Long getAfkTime() {
        return this.afkTime;
    }

    public Long getCurrentAfkTime() {
        if (CorePlayer.afkTimes.containsKey(this.uuid)) {
            Long add = System.currentTimeMillis() - CorePlayer.afkTimes.get(this.uuid);
            CorePlayer.afkTimes.put(this.uuid, System.currentTimeMillis());
            this.addAfkTime(add);
            return this.getAfkTime();
        } else {
            return this.getAfkTime();
        }
    }

    public void addAfkTime(Long add) {
        Long time = this.getAfkTime();
        this.setAfkTime(time + add);
        reload();
    }

    public void setAfkTime(Long time) {
        this.afkTime = time;
        reload();
    }

    // DISCORD ID
    public Long getDiscordId() {
        return this.discordId;
    }

    public void setDiscordUserId(Long id) {
        this.discordId = id;
        reload();
    }

    // DISCORD SYNCED ONCE
    public Boolean hasSyncedBefore() {
        return this.syncedOnce;
    }

    public void setSyncedOnce(Boolean bool) {
        this.syncedOnce = bool;
        reload();
    }

    // DISCORD IGNORING
    public Boolean isIgnoringDiscord() {
        return this.ignoresDiscord;
    }

    public void setIgnoringDiscord(Boolean bool) {
        this.ignoresDiscord = bool;
        reload();
    }

    // DISCORD IGNORING REMINDERS
    public Boolean isIgnoringDiscordReminder() {
        return this.ignoresDiscordReminders;
    }

    public void setIgnoringDiscordReminders(Boolean bool) {
        this.ignoresDiscordReminders = bool;
        reload();
    }

    // HOMES
    public void addHome(String name, Location loc) {
        int size = getHomes().size();
        if (size >= 1) {
            // Already has a home, we can loop through them
            for (String id : getHomes().keySet()) {
                if (name.equalsIgnoreCase(id)) {
                    // Home already exists, update it.
                    getHomes().put(name, StaticMethods.getPreciseLocString(loc));
                    getPlayer().sendMessage("§7Updated §b" + name + " §7to your location.");
                    reload();
                    return;
                }
            }

            // Home is new
            if (size <= getMaxHomes() - 1) {
                // Add it
                getHomes().put(name, StaticMethods.getPreciseLocString(loc));
                getPlayer().sendMessage("§7Saved §b" + name + " §7to your location.");
                reload();
            } else {
                getPlayer().sendMessage("§cYou cannot set any more homes. Max: §b" + getMaxHomes());
            }
        } else {
            // First home
            // Add it
            getHomes().put(name, StaticMethods.getPreciseLocString(loc));
            getPlayer().sendMessage("§7Saved §b" + name + " §7to your location.");
            getPlayer().sendMessage("§7Visit your home anytime using §b/home " + name);
            getPlayer().sendMessage("§7You can also set multiple homes.\n§7Example: §b/sethome other_home");
            getPlayer().sendMessage("§7View all your homes using §b/homes");
            reload();
        }
    }

    public void removeHome(String name) {
        if (getHomes().size() >= 1) {
            for (String id : getHomes().keySet()) {
                if (name.equalsIgnoreCase(id)) {
                    // found the home, remove it.
                    getHomes().remove(id);
                    getPlayer().sendMessage("§7Removed §b" + id + " §7from your homes.");
                    reload();
                    return;
                }
            }

            getPlayer().sendMessage("§cThat home could not be found.");
            getPlayer().sendMessage("§7Your homes: ");
            getPlayer().sendMessage("§b" + Joiner.on("§7, §b").join(getHomes().keySet()));
        }
    }

    public Map<String, String> getHomes() {
        return homes;
    }

    public void setHomes(Map<String, String> map) {
        this.homes = map;
        reload();
    }

    // LAST JOINED
    public Long getLastJoined() {
        return this.lastJoined;
    }

    public void setLastJoined(Long time) {
        this.lastJoined = time;
        reload();
    }

    // LAST QUIT
    public Long getLastQuit() {
        return this.lastQuit;
    }

    public void setLastQuit(Long time) {
        this.lastQuit = time;
        reload();
    }

    // LAST LOCATION
    public String getLastLocation() {
        return this.lastLocation;
    }

    public void setLastLocation(String loc) {
        this.lastLocation = loc;
        reload();
    }

    // VOTING TOTAL
    public Integer getTotalVotes() {
        return this.votes;
    }

    public void setTotalVotes(Integer total) {
        this.votes = total;
        reload();
    }

    public void incrementTotalVotes() {
        this.votes = votes + 1;
        reload();
    }

    // VOTING LAST VOTE
    public Long getLastVote() {
        return this.lastVote;
    }

    public void setLastVote(Long time) {
        this.lastVote = time;
        reload();
    }

    // VOTING THANKED
    public Boolean getThankedForOfflineVote() {
        return this.thankedForOfflineVote;
    }

    public void setThankedForOfflineVote(Boolean bool) {
        this.thankedForOfflineVote = bool;
        reload();
    }

    // BUKKIT/SPIGOT/ETC
    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    // 3RD PARTY
    // PROJECTKORRA
    public List<Element> getElements() {
        if (Compatibility.isHooked("ProjectKorra")) {
            return BendingPlayer.getBendingPlayer(this.getPlayer()).getElements();
        }

        // No elements, be sure to do a null/empty check.
        return new ArrayList<>();
    }

    // UTIL
    // LOAD
    public void loadData() {
        Configuration data = getData();
        if (data.get("UUID") == null) {
            saveData();
            reload();
        } else {
            // Update cPlayer from their Data file
            setUniqueId(UUID.fromString(data.getString("UUID")));
            setNick(data.getString("Nick"));
            setActiveTime(Long.valueOf(data.getString("Time.ActiveTime")));
            setAfkTime(Long.valueOf(data.getString("Time.AfkTime")));
            setDiscordUserId(Long.valueOf(data.getString("Discord.Id")));
            setSyncedOnce(data.getBoolean("Discord.SyncedOnce"));
            setIgnoringDiscord(data.getBoolean("Discord.Ignoring"));
            setIgnoringDiscordReminders(data.getBoolean("Discord.IgnoringReminders"));
            setLastJoined(Long.valueOf(data.getString("LastJoined")));
            setLastQuit(Long.valueOf(data.getString("LastQuit")));
            setLastLocation(data.getString("LastLocation"));
            setTotalVotes(data.getInteger("Voting.Total"));
            setLastVote(Long.valueOf(data.getString("Voting.LastVote")));
            setThankedForOfflineVote(data.getBoolean("Voting.Thanked"));
            if (data.get("Homes") != null) {
                for (String id : data.getYaml().getConfigurationSection("Homes").getKeys(false)) {
                    getHomes().put(id, data.getString("Homes." + id));
                }
            }

            // Thank them for voting if they have not been thanked.
            if (!getThankedForOfflineVote()) {
                // Thank them!
                getPlayer().sendMessage("§aThank you for voting!");
                setThankedForOfflineVote(true);
            }

            saveData();
            reload();
        }
    }

    public void reload() {
        // Update display name.
        getPlayer().setDisplayName(StaticMethods.addColor(this.nick));

        // Update tablist name.
        if (this.afkStatus) {
            getPlayer().setPlayerListName(StaticMethods.addColor("&7[AFK] " + PlayerMethods.getPlayerPrefix(getPlayer()) + this.name));
        } else {
            getPlayer().setPlayerListName(StaticMethods.addColor(PlayerMethods.getPlayerPrefix(getPlayer()) + this.name));
        }

        // Refresh the CorePlayer list.
        removePlayer(this);
        addPlayer(this);
    }

    // Only invoke if the player is online and is leaving.
    public void destroy() {
        UUID uuid = getUniqueId();

        if (isCurrentlyAfk()) {
            setAfkStatus(false, "Quitting");
        }

        if (DiscordManager.toggled) {
            if (!isIgnoringDiscord()) {
                DiscordManager.sendGameQuit(getPlayer());
            }
        }

        setActiveTime(getCurrentActiveTime());
        setAfkTime(getCurrentAfkTime());

        CorePlayer.activeTimes.remove(uuid);
        CorePlayer.afkTimes.remove(getUniqueId());

        Bukkit.getScheduler().cancelTask(AfkHandler.tasks.get(uuid));
        AfkHandler.lastAction.remove(uuid);
        AfkHandler.lastLocation.remove(uuid);
        AfkHandler.tasks.remove(uuid);

        WarmupTeleportTask.teleporting.remove(uuid);

        saveData();

        removePlayer(this);
    }

    public int getMaxHomes() {
        if (PlayerMethods.hasPermission(getPlayer(), "core.command.sethome", false)) {
            if (getPlayer().isOp()) {
                return 2147483647;
            }

            if (getPlayer().hasPermission("core.*")) {
                return 2147483647;
            }

            boolean hasNode = false;

            Set<PermissionAttachmentInfo> perms = getPlayer().getEffectivePermissions();

            ArrayList<Integer> possibleValues = new ArrayList<>();
            possibleValues.add(1);

            for (PermissionAttachmentInfo perm : perms) {
                String permission = perm.getPermission();

                if (permission.toLowerCase().startsWith("core.maxhomes.")) {
                    hasNode = true;
                    String args[] = permission.split("\\.");

                    if (permission.toLowerCase().equals("core.maxhomes.*")) {
                        return 2147483647;
                    }

                    try {
                        possibleValues.add(Integer.valueOf(args[2]));
                    } catch (Exception ex) {
                        net.doodcraft.oshcon.bukkit.enderpads.util.StaticMethods.debug("&eDiscovered an invalid permission node for &b" + getName() + "&e: &c" + permission);
                        possibleValues.add(1);
                    }
                }
            }

            if (hasNode) {
                return Collections.max(possibleValues);
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}