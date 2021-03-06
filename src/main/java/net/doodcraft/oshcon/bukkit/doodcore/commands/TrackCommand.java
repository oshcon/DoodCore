package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.tasks.CompassResetTask;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TrackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("track")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.track", true)) {
                    return false;
                }

                if (!player.hasPermission("core.bypass.vote")) {
                    if (!PlayerMethods.hasVotedToday(player)) {
                        if (!PlayerMethods.isSupporter(player)) {
                            player.sendMessage("§cUnlock this command for 24 hours by voting.");
                            player.sendMessage("§7See all the eligible voting sites using §b/vote");
                            return false;
                        }
                    }
                }

                if (args.length < 1) {
                    player.sendMessage("§7Track is used to locate another player, usually for PvP.");
                    player.sendMessage("§7It will attempt to point your compass in their general direction.");
                    player.sendMessage("§7Usage: §b/track <player>");
                    return false;
                }

                if (args.length == 1) {
                    if (hasEnoughQuartz(player)) {
                        if (CorePlayer.getPlayer(args[0]) != null) {

                            if (CorePlayer.getPlayer(args[0]).equals(player)) {
                                sender.sendMessage("§cYou cannot track yourself.");
                                return false;
                            }

                            if (CorePlayer.getPlayer(args[0]).hasPermission("core.command.track.untrackable")) {
                                player.sendMessage("§cThat player cannot be tracked right now.");
                                return false;
                            } else {
                                if (hasCompass(player)) {
                                    // point compass to a location within 50 blocks.
                                    // remove quartz
                                    if (player.getLocation().getWorld().equals(CorePlayer.getPlayer(args[0]).getLocation().getWorld())) {
                                        double xOffset = DoodCorePlugin.random.nextInt(100) - 50;
                                        double zOffset = DoodCorePlugin.random.nextInt(100) - 50;
                                        player.setCompassTarget(CorePlayer.getPlayer(args[0]).getLocation().add(xOffset, 0, zOffset));
                                        removeQuartz(player);
                                        new CompassResetTask(player).runTaskLater(DoodCorePlugin.plugin, 90 * 20);
                                        player.sendMessage("§aYour compass is now pointing towards their general location for ninety seconds.");
                                        return false;
                                    } else {
                                        player.sendMessage("§cThat player cannot be tracked right now.");
                                        return false;
                                    }
                                } else {
                                    player.sendMessage("§cYou need a compass for this to work.");
                                    return false;
                                }
                            }
                        } else {
                            player.sendMessage("§cThat player cannot be tracked right now.");
                            return false;
                        }
                    } else {
                        player.sendMessage("§cThis ability requires an ender eye to function.");
                        return false;
                    }
                }

                player.sendMessage("§cInvalid arguments.");
                player.sendMessage("§7Usage: §7b/track <player>");
                return false;
            } else {
                sender.sendMessage("Console cannot use this.");
                return false;
            }
        }
        return false;
    }

    public static Boolean hasCompass(Player player) {
        ItemStack[] inv = player.getInventory().getContents();

        int quantity = 0;

        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null) {
                if (inv[i].getType().equals(Material.COMPASS)){
                    int amount = inv[i].getAmount();
                    quantity = quantity + amount;
                }
            }
        }

        if (quantity >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean hasEnoughQuartz(Player player) {
        ItemStack[] inv = player.getInventory().getContents();

        int quantity = 0;

        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null) {
                if (inv[i].getType().equals(Material.EYE_OF_ENDER)){
                    int amount = inv[i].getAmount();
                    quantity = quantity + amount;
                }
            }
        }

        if (quantity >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void removeQuartz(Player player) {
        player.getInventory().removeItem(new ItemStack(Material.EYE_OF_ENDER, 1));
    }

    public static void resetCompass(Player player) {
        player.setCompassTarget(player.getWorld().getSpawnLocation());
    }
}
