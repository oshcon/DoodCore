package net.doodcraft.oshcon.bukkit.doodcore.coreplayer;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.badges.BadgeType;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Compatibility;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

public class RoleSync {

    public static void syncRank(CorePlayer cPlayer) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Member");

        if (cPlayer.getDiscordId() != 0L) {
            IUser user = DiscordManager.client.getUserByID(cPlayer.getDiscordId());
            if (user != null) {
                String rank = DiscordManager.getMatchingGameRank(DiscordManager.client.getGuildByID(Settings.discordGuild), user, cPlayer);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add " + rank);
                if (user.getRolesForGuild(DiscordManager.client.getGuildByID(Settings.discordGuild)).toString().contains("Supporter")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex user " + cPlayer.getName() + " group add Supporter");

                    Bukkit.getScheduler().runTaskLater(DoodCorePlugin.plugin, new Runnable() {
                        @Override
                        public void run() {
//                            cPlayer.addBadge(new Badge(BadgeType.SUPPORTER));
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
        if (cPlayer.getCurrentActiveTime() >= (Settings.veteranTime * 1000)) {
            // They need to be a Veteran now.

            if (!cPlayer.getCongratulatedVeteranRank()) {
                cPlayer.getPlayer().sendMessage("§6You've auto-ranked to §2§lVeteran§6, " + cPlayer.getName() + "! Congratulations!");
                cPlayer.getPlayer().sendMessage("§6In-game perks you've unlocked:");
                cPlayer.getPlayer().sendMessage("  §8- §aHome limit increase: §72 -> 6");
                cPlayer.getPlayer().sendMessage("  §8- §aEnderPad limit increase: §72 -> 10");
                cPlayer.getPlayer().sendMessage("  §8- §aNickname setting. §8[§b/nick§8]");
                cPlayer.getPlayer().sendMessage("  §8- §aChest locking in unclaimed territory.");
                cPlayer.getPlayer().sendMessage("  §8- §aAccess to marriage commands. §8[§b/marry§8]");
                cPlayer.setCongratulatedVeteranRank(true);
            }

            // Update their role on Discord. Let syncRank do the rest.
            // This requires their account to be synced to discord. Check if they are ignoring reminders.
            if (!Compatibility.isHooked("Vault") || Vault.permission == null || !Vault.permission.isEnabled()) {
                return;
            }

            if (cPlayer.isVeteran()) {
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
//                        cPlayer.addBadge(new Badge(BadgeType.SUPPORTER));
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
}
