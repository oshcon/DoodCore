package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordCommand implements CommandExecutor {

    public static Map<UUID, IUser> awaitingLinkReply = Collections.synchronizedMap(new ConcurrentHashMap<>());

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("discord")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (args.length == 0) {
                    sender.sendMessage(" ");
                    sender.sendMessage("§dJoin our Discord server! §ehttps://discord.gg/Z7GSpc4");
                    sender.sendMessage("§3Valid commands:");
                    sender.sendMessage("  §b/discord who: §7Display online Discord users");
                    sender.sendMessage("  §b/discord sync: §7Sync your Discord role and ID");
                    sender.sendMessage("  §b/discord unsync: §7Un-sync your Discord profile");
                    sender.sendMessage("  §b/discord hide: §7Hide all MC <-> Discord messages");
                    sender.sendMessage("  §b/discord reminder: §7Toggle Discord sync reminders");
                    return true;
                }

                // commands
                if (args[0].equalsIgnoreCase("who")) {
                    IGuild guild = DiscordManager.client.getGuildByID(Settings.discordGuild);

                    List<String> names = new ArrayList<>();
                    for (IUser user : guild.getUsers()) {
                        if (!user.getRolesForGuild(guild).toString().contains("Bots")) {
                            if (user.getPresence().getStatus() == StatusType.ONLINE || user.getPresence().getStatus() == StatusType.IDLE) {
                                names.add(DiscordManager.getDiscordRankPrefix(guild, user) + user.getName());
                            }
                        }
                    }

                    if (names.size() <= 0) {
                        sender.sendMessage("§7Nobody is on Discord right now..");
                        return true;
                    } else {
                        sender.sendMessage("§7Online (§b" + names.size() + "§7):  " + StringUtils.join(names, "§7, §r"));
                        return true;
                    }
                }


                if (args[0].equalsIgnoreCase("sync")) {

                    if (args.length == 1) {
                        sender.sendMessage(" ");
                        sender.sendMessage("§7First, join our Discord server: §e https://discord.gg/Z7GSpc4");
                        sender.sendMessage("§7Then supply your Discord username. (Not your nickname)");
                        sender.sendMessage("§3Correct Examples: ");
                        sender.sendMessage("§7    Name: §b/discord sync " + sender.getName());
                        sender.sendMessage("§7    Discriminator: §b/discord sync " + sender.getName() + "#1234");
                        sender.sendMessage("§7    Id: §b/discord sync 190189792942620672");
                        return false;
                    }

                    for (IUser user : DiscordManager.client.getUsers()) {
                        List<String> userArgs = new ArrayList<>(Arrays.asList(args));
                        userArgs.remove(args[0]);

                        if (user.getName().equalsIgnoreCase(Joiner.on(" ").join(userArgs))) {
                            return trySync(sender, user);
                        }
                        if ((user.getName() + "#" + user.getDiscriminator()).equalsIgnoreCase(Joiner.on(" ").join(userArgs))) {
                            return trySync(sender, user);
                        }
                        if (String.valueOf(user.getLongID()).equalsIgnoreCase(Joiner.on(" ").join(userArgs))) {
                            return trySync(sender, user);
                        }
                    }

                    sender.sendMessage(" ");
                    sender.sendMessage("§cThat user could not be found. Have you joined our Discord server?");
                    sender.sendMessage("§7If you continue to have issues, contact a member of staff.");
                    sender.sendMessage("§3Correct Examples: ");
                    sender.sendMessage("§7    Name: §b/discord sync " + sender.getName());
                    sender.sendMessage("§7    Discriminator: §b/discord sync " + sender.getName() + "#1234");
                    sender.sendMessage("§7    Id: §b/discord sync 190189792942620672");
                    return false;
                }

                if (args[0].equalsIgnoreCase("unsync")) {
                    CorePlayer cPlayer = CorePlayer.players.get(((Player) sender).getUniqueId());
                    if (cPlayer != null) {
                        if (cPlayer.getDiscordUserId() != 0) {
                            DiscordManager.unSyncDiscord(cPlayer);
                            sender.sendMessage("§7Un-synced your Discord account successfully!");
                            return true;
                        } else {
                            // Doesn't have an ID. Check the ID map. Clean it all out and reset to be safe.
                            DiscordManager.unSyncDiscord(cPlayer);
                            sender.sendMessage("§7You have not synced a Discord account yet.");
                            return true;
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("hide")) {
                    CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
                    if (cPlayer.isIgnoringDiscord()) {
                        cPlayer.setIgnoresDiscord(false);
                        player.sendMessage("§7You are no longer hiding Discord messages.");
                        return true;
                    } else {
                        cPlayer.setIgnoresDiscord(true);
                        player.sendMessage("§7All Discord messages will now be hidden until you enable them again.");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("reminder")) {
                    CorePlayer cPlayer = CorePlayer.players.get(player.getUniqueId());
                    if (cPlayer.isIgnoringDiscordReminder()) {
                        cPlayer.setIgnoresDiscordReminder(false);
                        player.sendMessage("§7You are no longer ignoring sync reminders.");
                        DiscordManager.startReminderTask(cPlayer);
                        return true;
                    } else {
                        cPlayer.setIgnoresDiscordReminder(true);
                        player.sendMessage("§7You will no longer receive Discord sync reminders.");
                        return true;
                    }
                }

                sender.sendMessage(" ");
                sender.sendMessage("§cInvalid command.");
                sender.sendMessage("§3Valid commands:");
                sender.sendMessage("  §b/discord who: §7Display online Discord users");
                sender.sendMessage("  §b/discord sync: §7Sync your Discord role and ID");
                sender.sendMessage("  §b/discord unsync: §7Un-sync your Discord profile");
                sender.sendMessage("  §b/discord hide: §7Hide all MC <-> Discord messages");
                sender.sendMessage("  §b/discord reminder: §7Toggle Discord sync reminders");
                return false;
            } else {

                if (args.length == 0) {
                    sender.sendMessage("Invalid command.");
                    return true;
                }

                // commands
                if (args[0].equalsIgnoreCase("who")) {
                    IGuild guild = DiscordManager.client.getGuildByID(Settings.discordGuild);

                    List<String> names = new ArrayList<>();
                    for (IUser user : guild.getUsers()) {
                        if (!user.getRolesForGuild(guild).toString().contains("Bots")) {
                            if (user.getPresence().getStatus() == StatusType.ONLINE || user.getPresence().getStatus() == StatusType.IDLE) {
                                names.add(DiscordManager.getDiscordRankPrefix(guild, user) + user.getName());
                            }
                        }
                    }

                    if (names.size() <= 0) {
                        sender.sendMessage("§7Nobody is online.");
                        return true;
                    } else {
                        sender.sendMessage("§7Online (§b" + names.size() + "§7): " + StringUtils.join(names, "§7, §r"));
                        return true;
                    }
                }

                sender.sendMessage("Invalid command.");
                return false;
            }
        }
        return false;
    }

    private static boolean trySync(CommandSender sender, IUser user) {
        // Check if there is already a pairing.
        if (CorePlayer.players.get(((Player) sender).getUniqueId()).getDiscordUserId() != 0L) {
            sender.sendMessage("§7You have already synced with a Discord account. If you need to change this, contact a member of staff.");
            return false;
        }
        // Ensure a 1:1 relationship. Two players cannot request the same user.
        if (awaitingLinkReply.containsValue(user)) {
            sender.sendMessage("§7Another player already sent a request to that user. You must wait for them to accept, deny, cancel, or let their request expire.");
            return false;
        }
        // Check the idmap for if the user is already linked to a player.
        if (DiscordManager.idMap().getKeys(false).contains(user.getStringID())) {
            sender.sendMessage("§7The user you specified is already synced with another player. If you need to change this, contact a staff member.");
            return false;
        }
        // Prevent the player from having two user requests. Also ensuring a 1:1 relationship. Now our map is "bi-directional" :)
        if (awaitingLinkReply.containsKey(((Player) sender).getUniqueId())) {
            sender.sendMessage("§7Cancelled your previous request to " + awaitingLinkReply.get(((Player) sender).getUniqueId()).getName() + ".");
            awaitingLinkReply.remove(((Player) sender).getUniqueId());
        }
        try {
            user.getOrCreatePMChannel().sendMessage( sender.getName() + " from  *`mc.doodcraft.net`*  is attempting to sync your Discord ID and user rank to their in-game profile. If you sent this request, reply to this message with a `yes`. If you did not authorize this request or are unsure what this is, ignore this message, or just type `no`.");
            awaitingLinkReply.put(((Player) sender).getUniqueId(), user);
            sender.sendMessage("§7Sync request sent! Check Discord for a message from the bot.");
            return true;
        } catch (Exception ex) {
            sender.sendMessage("§7There was an error sending a sync request to that user. You may not have permission.");
            sender.sendMessage("§eError: §7" + ex.getLocalizedMessage());
            return false;
        }
    }
}