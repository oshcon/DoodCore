package net.doodcraft.oshcon.bukkit.doodcore.tasks;

import fr.xephi.authme.api.v3.AuthMeApi;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.badges.BadgeType;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.config.Settings;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.RoleSync;
import net.doodcraft.oshcon.bukkit.doodcore.discord.MinecraftMessages;
import net.doodcraft.oshcon.bukkit.doodcore.listeners.VotifierListener;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerUpdateTask extends BukkitRunnable {

    UUID uuid;
    Boolean motd = false;
    Boolean joined = false;

    public PlayerUpdateTask(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        if (Bukkit.getPlayer(this.uuid) != null) {
            if (CorePlayer.getPlayers().containsKey(this.uuid)) {
                CorePlayer cPlayer = CorePlayer.getPlayers().get(this.uuid);

                if (!AuthMeApi.getInstance().isAuthenticated(cPlayer.getPlayer())) {
                    return;
                }

                if (!motd) {
                    Messages.sendMultiLine(cPlayer, "MOTD");
                    motd = true;
                }

                if (!joined) {
                    MinecraftMessages.broadcastJoin(cPlayer);
                    joined = true;
                }

                Long l = cPlayer.getCurrentActiveTime();

                // check pvp expiration
                if (l >= (Settings.pvpProtection * 1000)) {
                    if (cPlayer.getPlayer() != null) {
                        // Warn them.
                        if (!cPlayer.getWarnedPVPExpiration()) {
                            cPlayer.getPlayer().sendMessage("§cYour PvP protection has expired!");
                            cPlayer.setWarnedPVPExpiration(true);
                        }
                    }
                }

                // check if veteran
                if (l >= (Settings.veteranTime * 1000)) {
                    RoleSync.autoRankVeteran(cPlayer);
                } else {
                    if (cPlayer.getCongratulatedVeteranRank()) {
                        cPlayer.setCongratulatedVeteranRank(false);
                    }
                }

                // payout
                payout(cPlayer);

                // award conditional badges
                awardBadges(cPlayer);

                // award vote flares
                VotifierListener.giveVoteFlares(cPlayer);
            }

            return;
        }

        this.cancel();
    }

    public static void awardBadges(CorePlayer cPlayer) {

        if (!cPlayer.hasBadge(BadgeType.BETA_TESTER)) {
            cPlayer.addBadge(new Badge(BadgeType.BETA_TESTER));
        }

        if (!cPlayer.hasBadge(BadgeType.SUPPORTER)) {
            if (PlayerMethods.isSupporter(cPlayer.getPlayer())) {
                cPlayer.addBadge(new Badge(BadgeType.SUPPORTER));
            }
        }

        if (!cPlayer.hasBadge(BadgeType.PLAYER_SLAYER)) {
            if (cPlayer.getPlayerKills().size() >= 50) {
                cPlayer.addBadge(new Badge(BadgeType.PLAYER_SLAYER));
            }
        }
    }

    public static void payout(CorePlayer cPlayer) {
        // get last payout time, if it's 30 minutes or greater since their last payout, perform a new payout
        if (cPlayer.timeToNextPayout() <= 0L) {

            // payout Ƶ50/30m or (1 2/3)/m

            double add = StaticMethods.round(((double) ((cPlayer.getCurrentActiveTime() - cPlayer.getLastPayout())/60000L)) * 1.66666666666667, 2);

            Vault.economy.depositPlayer(cPlayer.getPlayer(), add);

            cPlayer.getPlayer().sendMessage("§aYou just earned §6§l" + add + "Ƶ§a for being active! §8[§b/bal§8] [§b/mytime§8]");

            cPlayer.setLastPayout(cPlayer.getCurrentActiveTime());
        }
    }
}