package net.doodcraft.oshcon.bukkit.doodcore.coreplayer;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.afk.AfkHandler;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CorePlayer {

    public static Map<UUID, CorePlayer> players = new ConcurrentHashMap<>();
    public static Map<UUID, Long> activeTimes = new ConcurrentHashMap<>();
    public static Map<UUID, Long> afkTimes = new ConcurrentHashMap<>();

    private UUID uuid;
    private String name;
    private String nickName;
    private Long discordUserId;
    private Boolean syncedOnce;
    private Boolean ignoresDiscord;
    private Boolean ignoresDiscordReminder;
    private Boolean afkStatus;
    private Long activeTime;
    private Long afkTime;

    // -- CREATE
    public CorePlayer(UUID uuid) {
        // Default values for a generic CorePlayer object.
        this.uuid = uuid;
        this.name = Bukkit.getPlayer(uuid).getName();
        this.nickName = this.name;
        this.discordUserId = 0L;
        this.syncedOnce = false;
        this.ignoresDiscord = false;
        this.ignoresDiscordReminder = false;
        this.afkStatus = false;
        this.activeTime = 0L;
        this.afkTime = 0L;

        if (players.get(uuid) == null) {
            players.put(uuid, this);
        } else {
            players.remove(uuid);
            players.put(uuid, this);
        }

        save();
    }

    // -- SET
    public void setName(String name) {
        this.name = name;
    }

    public void setNickName(String nick) {
        this.nickName = nick;
        reload(this);
    }

    public void setDiscordUserId(Long id) {
        this.discordUserId = id;
        reload(this);
    }

    public void setSyncedOnce(Boolean bool) {
        this.syncedOnce = bool;
        reload(this);
    }

    public void setIgnoresDiscord(Boolean ignores) {
        this.ignoresDiscord = ignores;
        reload(this);
    }

    public void setIgnoresDiscordReminder(Boolean ignores) {
        this.ignoresDiscordReminder = ignores;
        reload(this);
    }

    public void setAfkStatus(Boolean status, String reason) {
        if (status) {
            if (!this.afkStatus) {

                this.afkStatus = true;
                afkTimes.putIfAbsent(this.uuid, System.currentTimeMillis());
                this.setActiveTime(this.getCurrentActiveTime());
                activeTimes.remove(this.uuid);

                if (reason == null) {
                    Bukkit.broadcastMessage(Vault.chat.getPlayerPrefix(getPlayer()) + Bukkit.getPlayer(this.uuid).getDisplayName() + " §7is now AFK.");
                } else {
                    Bukkit.broadcastMessage(Vault.chat.getPlayerPrefix(getPlayer()) + Bukkit.getPlayer(this.uuid).getDisplayName() + " §7is now AFK. [§3" + reason + "§7]");
                }
            }
        } else {
            if (this.afkStatus) {

                this.afkStatus = false;
                this.setAfkTime(this.getCurrentAfkTime());
                afkTimes.remove(this.uuid);
                CorePlayer.activeTimes.putIfAbsent(this.uuid, System.currentTimeMillis());

                Bukkit.broadcastMessage(Vault.chat.getPlayerPrefix(getPlayer()) + Bukkit.getPlayer(this.uuid).getDisplayName() + " §7is no longer AFK.");
            }
            AfkHandler.lastAction.put(this.uuid, System.currentTimeMillis());
        }
    }

    public void addActiveTime(Long add) {
        this.setActiveTime(this.getActiveTime() + add);
    }

    public void setActiveTime(Long time) {
        this.activeTime = time;
        reload(this);
    }

    public void addAfkTime(Long add) {
        Long time = this.getAfkTime();
        this.setAfkTime(time + add);
    }

    public void setAfkTime(Long time) {
        this.afkTime = time;
        reload(this);
    }

    // -- GET
    public UUID getUniqueId() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {
        return this.nickName;
    }

    public Long getDiscordUserId() {
        return this.discordUserId;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public Long getActiveTime() {
        return this.activeTime;
    }

    public Long getAfkTime() {
        return this.afkTime;
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

    public Configuration getDataFile() {
        return new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + this.uuid.toString() + ".yml");
    }

    // -- IS
    public Boolean hasSyncedBefore() {
        return this.syncedOnce;
    }

    public Boolean isIgnoringDiscord() {
        return this.ignoresDiscord;
    }

    public Boolean isIgnoringDiscordReminder() {
        return this.ignoresDiscordReminder;
    }

    public Boolean isCurrentlyAfk() {
        return this.afkStatus;
    }

    public static void createCorePlayer(Player player) {
        Configuration data = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + player.getUniqueId().toString() + ".yml");
        if (data.get("UUID") == null) {
            CorePlayer cPlayer = new CorePlayer(player.getUniqueId());
            reload(cPlayer);
        } else {
            CorePlayer cPlayer = new CorePlayer(player.getUniqueId());
            cPlayer.setName(data.getString("Name.RealName"));
            cPlayer.setNickName(data.getString("Name.NickName"));
            cPlayer.setDiscordUserId((long) data.getInteger("Discord.Id"));
            cPlayer.setSyncedOnce(data.getBoolean("Discord.SyncedOnce"));
            cPlayer.setIgnoresDiscord(data.getBoolean("Discord.Ignoring"));
            cPlayer.setIgnoresDiscordReminder(data.getBoolean("Discord.IgnoringReminder"));
            cPlayer.setActiveTime((long) data.getInteger("Time.ActiveTime"));
            cPlayer.setAfkTime((long) data.getInteger("Time.AfkTime"));
            reload(cPlayer);
        }
    }

    // Attach 3rd party plugin data for easier retrieval
    // ProjectKorra
    public List<Element> getElements() {
        if (Compatibility.isHooked("ProjectKorra")) {
            return BendingPlayer.getBendingPlayer(this.getPlayer()).getElements();
        }

        // No elements, be sure to do a null/empty check.
        return new ArrayList<>();
    }

    // Towny

    // MarriageMaster

    // McMMO

    public void save() {
        Configuration data = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + this.uuid.toString() + ".yml");
        data.set("UUID", this.uuid.toString());
        data.set("Name.RealName", this.name);
        data.set("Name.NickName", this.nickName);
        data.set("Discord.Id", this.discordUserId);
        data.set("Discord.SyncedOnce", this.syncedOnce);
        data.set("Discord.Ignoring", this.ignoresDiscord);
        data.set("Discord.IgnoringReminder", this.ignoresDiscordReminder);
        data.set("Time.ActiveTime", this.activeTime);
        data.set("Time.AfkTime", this.afkTime);
        data.save();
    }

    public static void reload(CorePlayer cPlayer) {

        cPlayer.getPlayer().setDisplayName(StaticMethods.addColor(cPlayer.getNickName()));

        if (cPlayer.isCurrentlyAfk()) {
            cPlayer.getPlayer().setPlayerListName(StaticMethods.addColor("§7[AFK] " + Vault.chat.getPlayerPrefix(cPlayer.getPlayer()) + cPlayer.getName()));
        } else {
            cPlayer.getPlayer().setPlayerListName(StaticMethods.addColor(Vault.chat.getPlayerPrefix(cPlayer.getPlayer()) + cPlayer.getName()));
        }

        if (players.containsKey(cPlayer.getUniqueId())) {
            players.remove(cPlayer.getUniqueId());
            players.put(cPlayer.getUniqueId(), cPlayer);
        } else {
            players.put(cPlayer.getUniqueId(), cPlayer);
        }
    }

    public static void destroy(CorePlayer cPlayer) {
        if (players.containsKey(cPlayer.getUniqueId())) {
            players.remove(cPlayer.getUniqueId());
            cPlayer.save();
        } else {
            // Not sure why the cPlayer wouldn't be in the map, but jic.
            cPlayer.save();
        }
    }
}