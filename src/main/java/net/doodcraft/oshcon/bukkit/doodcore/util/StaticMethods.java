package net.doodcraft.oshcon.bukkit.doodcore.util;

import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticMethods {

    private static SimpleDateFormat format = new SimpleDateFormat("hh:mm");

    public static String getTimeStamp() {
        return format.format(new Date());
    }

    public static String getLocString(Location loc) {
        return loc.getWorld().getName() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ();
    }

    public static int purgeMonsters(World world) {
        int size = 0;

        for (Entity e : world.getEntities()) {
            if (e instanceof Monster) {
                if (e.getCustomName() == null) {
                    Bukkit.getPluginManager().callEvent(new EntityDeathEvent((LivingEntity) e, null));
                    e.remove();
                    size++;
                }
            }
        }

        return size;
    }

    public static int purgeItemDrops(World world) {
        int size = 0;

        for (Entity e : world.getEntities()) {
            if (e instanceof Item) {
                ItemStack stack = ((Item) e).getItemStack();

                if (!stack.getItemMeta().hasDisplayName()) {
                    if (!stack.getItemMeta().hasEnchants()) {
                        size = size + ((Item) e).getItemStack().getAmount();
                        e.remove();
                    }
                }
            }
        }

        return size;
    }

    public static void log(String message) {
        try {
            message = "[" + DoodCorePlugin.plugin.getDescription().getName() + "] &r" + message;
            sendConsole(message);
        } catch (Exception ex) {
            Logger logger = Bukkit.getLogger();
            logger.log(Level.INFO, removeColor("[" + DoodCorePlugin.plugin.getDescription().getName() + "] " + message));
        }
    }

    private static void sendConsole(String message) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(addColor(message));
    }

    public static String addColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String removeColor(String message) {
        message = addColor(message);
        return ChatColor.stripColor(message);
    }
}
