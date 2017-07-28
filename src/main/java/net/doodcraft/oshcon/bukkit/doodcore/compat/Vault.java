package net.doodcraft.oshcon.bukkit.doodcore.compat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

    public static Chat chat;
    public static Permission permission;
    public static Economy economy;

    public static boolean isInstalled() {
        return Compatibility.isHooked("Vault");
    }

    public static boolean setupChat() {
        if (!isInstalled()) {
            return false;
        }

        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider == null) {
            return false;
        }
        chat = chatProvider.getProvider();
        return chat != null;
    }

    public static boolean setupEconomy() {
        if (!isInstalled()) {
            return false;
        }

        RegisteredServiceProvider<Economy> econProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (econProvider == null) {
            return false;
        }
        economy = econProvider.getProvider();
        return economy != null;
    }

    public static boolean setupPermissions() {
        if (!isInstalled()) {
            return false;
        }

        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider == null) {
            return false;
        }
        permission = permissionProvider.getProvider();
        return permission != null;
    }
}
