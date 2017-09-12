package net.doodcraft.oshcon.bukkit.doodcore.commands;

import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("head")) {
            if (sender instanceof Player) {

                Player player = (Player) sender;

                if (!PlayerMethods.hasPermission(player, "core.command.head", true)) {
                    return false;
                }

                if (args.length != 1) {
                    player.sendMessage("Invalid args.");
                    return false;
                }

                player.getInventory().addItem(getPlayerHead(args[0]));
                player.sendMessage("Giving head. ;)");
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }

    public static ItemStack getPlayerHead(String name) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwner(name);
        headMeta.setDisplayName("§r" + name + "'s Head");
        head.setDurability((short) 3);
        head.setItemMeta(headMeta);
        return head;
    }
}