package net.doodcraft.oshcon.bukkit.doodcore.commands;

import com.google.common.base.Joiner;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.config.Messages;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.discord.DiscordManager;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import org.bukkit.Bukkit;
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
                CorePlayer cPlayer = CorePlayer.getPlayers().get(player.getUniqueId());

                if (!PlayerMethods.hasPermission(player, "core.command.head", true)) {
                    return false;
                }

                if (args.length != 1) {
                    player.sendMessage("Invalid args.");
                    return false;
                }

                ItemStack head = new ItemStack(Material.SKULL_ITEM, (short) 3);
                SkullMeta headMeta = (SkullMeta) head.getItemMeta();
                headMeta.setOwner(args[0]);
                headMeta.setDisplayName(args[0]);
                head.setDurability((short) 3);
                head.setItemMeta(headMeta);

                player.getInventory().addItem(head);
                player.sendMessage("Giving head. ;)");
                return true;
            } else {
                sender.sendMessage("Console can't use this command.");
                return false;
            }
        }
        return false;
    }
}