package net.doodcraft.oshcon.bukkit.doodcore.commands;

import mkremins.fanciful.FancyMessage;
import net.doodcraft.oshcon.bukkit.doodcore.badges.Badge;
import net.doodcraft.oshcon.bukkit.doodcore.badges.BadgeType;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BadgesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("badges")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.badges", true)) {
                    return false;
                }

                if (args.length > 0) {

                    // Show available badges with descriptions
                    if (args[0].equals("list")) {
                        player.sendMessage("§7§lEarnable Badges:");
                        for (BadgeType type : BadgeType.values()) {
                            if (BadgeType.isBadgeType(type.toString())) {
                                Badge b = new Badge(type);
                                player.sendMessage("§8--------");
                                player.sendMessage("§8[" + b.getFriendlyName() + "§8]");
                                player.sendMessage("§f" + b.getDescription());
                                player.sendMessage("§8--------");
                            }
                        }
                        return true;
                    }

                    if (player.hasPermission("core.command.badges.admin")) {
                        // Display another players badges
                        if (args[0].equalsIgnoreCase("display")) {
                            if (CorePlayer.getPlayer(args[1]) != null) {
                                CorePlayer target = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[1]).getUniqueId());
                                if (target.getBadges().size() > 0) {
                                    player.sendMessage("§7" + target.getName() + "'s Badges: ");
                                    for (Badge b : target.getBadges()) {
                                        FancyMessage msg = new FancyMessage("  §8- ");
                                        msg.then(b.getFriendlyName()).tooltip(b.getDescription());
                                        msg.send(player);
                                    }
                                } else {
                                    player.sendMessage("§cThey have no badges.");
                                    return false;
                                }
                            } else {
                                player.sendMessage("§cThat player is not online.");
                                return false;
                            }
                        }
                        // Add
                        if (args[0].equalsIgnoreCase("add")) {
                            if (CorePlayer.getPlayer(args[1]) != null) {
                                CorePlayer target = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[1]).getUniqueId());
                                if (BadgeType.isBadgeType(args[2].toUpperCase())) {
                                    Badge badge = new Badge(args[2].toUpperCase());
                                    if (!target.hasBadge(badge)) {
                                        target.addBadge(badge);
                                        player.sendMessage("§aAdded.");
                                        return true;
                                    } else {
                                        player.sendMessage("§cThey already have that badge.");
                                        return true;
                                    }
                                } else {
                                    player.sendMessage("§cThat is not a valid badge.");
                                    return false;
                                }
                            } else {
                                player.sendMessage("§cThat player is not online.");
                                return false;
                            }
                        }
                        // Remove
                        if (args[0].equalsIgnoreCase("remove")) {
                            if (CorePlayer.getPlayer(args[1]) != null) {
                                CorePlayer target = CorePlayer.getPlayers().get(CorePlayer.getPlayer(args[1]).getUniqueId());
                                if (BadgeType.isBadgeType(args[2].toUpperCase())) {
                                    Badge badge = new Badge(args[2].toUpperCase());
                                    if (target.hasBadge(badge)) {
                                        target.removeBadge(badge);
                                        player.sendMessage("§aRemoved.");
                                        return true;
                                    } else {
                                        player.sendMessage("§cThey don't have that badge.");
                                        return true;
                                    }
                                } else {
                                    player.sendMessage("§cThat is not a valid badge.");
                                    return false;
                                }
                            } else {
                                player.sendMessage("§cThat player is not online.");
                                return false;
                            }
                        }
                    }
                }

                // Display their badges, show the help
                player.sendMessage("§7Badges help you stand out by signifying your special achievements.");
                player.sendMessage("§7They are displayed in chat whenever somebody hovers over your name.");
                player.sendMessage("§7View the badges you can earn with §b/badges list");

                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());
                player.sendMessage("§7§lYour Badges:");
                if (cPlayer.getBadges().size() > 0) {
                    for (Badge b : cPlayer.getBadges()) {
                        FancyMessage msg = new FancyMessage("  §8- " + b.getFriendlyName());
                        msg.tooltip(b.getDescription());
                        msg.send(cPlayer.getPlayer());
                    }
                } else {
                    player.sendMessage("§cYou haven't earned any badges yet!");
                }

                return false;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}