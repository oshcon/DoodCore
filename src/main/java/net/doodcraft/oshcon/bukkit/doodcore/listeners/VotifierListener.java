package net.doodcraft.oshcon.bukkit.doodcore.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import de.slikey.effectlib.effect.FountainEffect;
import de.slikey.effectlib.util.ParticleEffect;
import net.doodcraft.oshcon.bukkit.doodcore.DoodCorePlugin;
import net.doodcraft.oshcon.bukkit.doodcore.compat.Vault;
import net.doodcraft.oshcon.bukkit.doodcore.config.Configuration;
import net.doodcraft.oshcon.bukkit.doodcore.coreplayer.CorePlayer;
import net.doodcraft.oshcon.bukkit.doodcore.util.NumberConverter;
import net.doodcraft.oshcon.bukkit.doodcore.util.PlayerMethods;
import net.doodcraft.oshcon.bukkit.doodcore.util.StaticMethods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class VotifierListener implements Listener {

    // TODO: Broadcast vote to Discord

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        UUID uuid;

        uuid = PlayerMethods.getCrackedUUID(vote.getUsername());

        StaticMethods.log("Received vote for " + vote.getUsername() + " from " + vote.getServiceName() + ":" + vote.getAddress());

        // Check each online player's name for case insensitivity.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().equals(vote.getUsername().toLowerCase())) {
                uuid = PlayerMethods.getCrackedUUID(p.getName());
            }
        }

        if (Bukkit.getPlayer(uuid) != null) {
            // They are online, message them.
            CorePlayer cPlayer = CorePlayer.getPlayers().get(uuid);
            cPlayer.getPlayer().sendMessage("§7Thank you for voting at " + vote.getServiceName() + ", " + cPlayer.getName() + "!");

            // check if first vote in 24 hour period
            if ((System.currentTimeMillis() - cPlayer.getLastVote()) >= 86400 * 1000L) { // greater than millis in 24 hours
                // its been more than 24 hours since the last vote, tell them theyve unlocked stuff
                Bukkit.broadcastMessage("§d" + cPlayer.getColorPrefix() + cPlayer.getNick() + " §djust voted for the server! §8[§b/vote§8]");
                cPlayer.getPlayer().sendMessage("§7You've unlocked §b/home§7, §b/back§7, §b/tpa§7, and §b/tpahere");
            }

            cPlayer.setLastVote(System.currentTimeMillis());
            cPlayer.setThankedForOfflineVote(true);
            cPlayer.incrementTotalVotes();
        } else {
            // They are not online, modify data directly.
            Configuration data = new Configuration(DoodCorePlugin.plugin.getDataFolder() + File.separator + "data" + File.separator + (uuid + ".yml"));
            // If the UUID/name needs to be added, it's likely the player made a typo in their name.
            // This can be solved more easily when I write my own vote listener in node.js

            if (data.get("UUID") == null) {
                StaticMethods.log("Received a vote for a non-existent player, " + vote.getUsername() + "! Could be a typo? Warn them of this.");
            }

            data.add("UUID", uuid);
            data.add("Name", vote.getUsername());
            data.set("Voting.LastVote", System.currentTimeMillis());
            data.set("Voting.Thanked", false);
            data.set("Voting.Total", data.getInteger("Voting.Total") + 1);
            data.save();

            Bukkit.broadcastMessage("§7" + vote.getUsername() + " §djust voted for the server! §8[§b/vote§8]");
        }
    }

    public static void giveVoteFlares(CorePlayer cPlayer) {
        int votes = cPlayer.getTotalVotes();
        int given = cPlayer.getTotalFlaresGiven();
        int owed = votes - given;
        int add = 0;

        if (owed >= 1) {
            if (cPlayer.getPlayer().getInventory().firstEmpty() != -1) {

                int doubleChance = DoodCorePlugin.random.nextInt(100) + 1;

                if (doubleChance <= 5) {
                    add = owed*3;
                    // TRIPLE FLARES
                    Bukkit.broadcastMessage("§7" + cPlayer.getColorPrefix() + cPlayer.getNick() + " §7was extra lucky and received triple vote flares!");
                }

                if ((doubleChance >= 6 && doubleChance <= 15) || (PlayerMethods.isSupporter(cPlayer.getPlayer()) && doubleChance > 5)) {
                    add = owed*2;
                    // DOUBLE FLARES
                    Bukkit.broadcastMessage("§7" + cPlayer.getColorPrefix() + cPlayer.getNick() + " §7was lucky and received double vote flares!");
                }

                if (add == 0) {
                    add = owed;
                }

                // They are owed vote flares
                if (add > 1) {
                    cPlayer.getPlayer().sendMessage("§7You earned §6§l" + NumberConverter.convert(add) + " §7vote flares.");
                } else {
                    cPlayer.getPlayer().sendMessage("§7You earned §6§lone §7vote flare.");
                }

                cPlayer.getPlayer().getInventory().addItem(getVoteFlareItem(cPlayer.getPlayer(), add));
                cPlayer.getPlayer().updateInventory();

                cPlayer.setTotalFlaresGiven(given + owed);
            }
        }
    }

    public static ItemStack getVoteFlareItem(Player player, int amount) {
        List<String> lore = new ArrayList<>();
        lore.add("§bPlace me on the ground");
        lore.add("§bto win a random reward!");
        lore.add(" ");
        if (player != null) {
            lore.add("§7Thank you for voting");
            lore.add("§7for §3Dood§7Craft§7, " + PlayerMethods.getPlayerPrefix(player) + player.getName());
            lore.add(" ");
        }
        lore.add("§8" + String.valueOf(System.currentTimeMillis()));

        ItemStack flare = new ItemStack(Material.REDSTONE_TORCH_ON);

        ItemMeta flareMeta = flare.getItemMeta();
        flareMeta.setDisplayName("§3§lVote Flare");
        flareMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        flareMeta.setLore(lore);

        flare.setItemMeta(flareMeta);
        flare.setAmount(amount);

        return flare;
    }

    public static Boolean runFlareReward(ItemStack flare, Location location, CorePlayer cPlayer) {

        if (!cPlayer.getPlayer().hasPermission("core.voteflares.use")) {
            return false;
        }

        if (PlayerMethods.canBuild(cPlayer.getPlayer(), location.getBlock())) {

            FountainEffect effect = new FountainEffect(DoodCorePlugin.effectManager);
            effect.particle = ParticleEffect.FIREWORKS_SPARK;
            effect.setLocation(location.add(0.5, 0, 0.5));
            effect.duration = 1750;
            effect.strands = 10;
            effect.particlesStrand = 3;
            effect.particlesSpout = 7;
            effect.radius = 4;
            effect.radiusSpout = 0.1F;
            effect.heightSpout = 2.5F;
            effect.start();

            String owner = "";

            for (String line : flare.getItemMeta().getLore()) {
                if (line.contains("§3Dood§7Craft§7,")) {
                    owner = StaticMethods.removeColor(line.split(" ")[2]);
                }
            }

            String flareId = StaticMethods.removeColor(flare.getItemMeta().getLore().get(flare.getItemMeta().getLore().size() - 1));

            if (owner.equals("")) {
                StaticMethods.log(cPlayer.getName() + " is redeeming a vote flare, ID: " + flareId);
            } else {
                StaticMethods.log(cPlayer.getName() + " is redeeming " + owner + "'s vote flare, ID: " + flareId);
            }

            location.getWorld().strikeLightningEffect(location);
            location.getBlock().setType(Material.AIR);

            Random rand = DoodCorePlugin.random;

            int odds = rand.nextInt(100) + 1;

            if (odds <= 17) {
                // enchantments
                int chance = rand.nextInt(10000) + 1;

                if (chance <= 80) {
                    // mending legendary
                    givePrize(cPlayer, location, 1);
                }
                if (chance >= 81 && chance <= 500) {
                    // unbreaking III rare
                    givePrize(cPlayer, location, 2);
                }
                if (chance >= 501 && chance <= 1500) {
                    // fortune II rare
                    givePrize(cPlayer, location, 3);
                }
                if (chance >= 1501 && chance <= 3200) {
                    // protection II uncommon
                    givePrize(cPlayer, location, 4);
                }
                if (chance >= 3201 && chance <= 5200) {
                    // efficiency II uncommon
                    givePrize(cPlayer, location, 5);
                }
                if (chance >= 5201 && chance <= 7500) {
                    // sharp II common
                    givePrize(cPlayer, location, 6);
                }
                if (chance >= 7501 && chance <= 10000) {
                    // power II common
                    givePrize(cPlayer, location, 7);
                }
            }

            if (odds >= 18 && odds <= 54) {
                // items
                int chance = rand.nextInt(1000) + 1;

                if (chance <= 5) {
                    // 1 notch apple legendary
                    givePrize(cPlayer, location, 8);
                }

                if (chance >= 6 && chance <= 20) {
                    // 1 nether star legendary
                    givePrize(cPlayer, location, 9);
                }

                if (chance >= 21 && chance <= 50) {
                    // elytra rare
                    givePrize(cPlayer, location, 10);
                }

                if (chance >= 51 && chance <= 90) {
                    // 1 wither skull rare
                    givePrize(cPlayer, location, 11);
                }

                if (chance >= 91 && chance <= 130) {
                    // 4 dragons breath rare
                    givePrize(cPlayer, location, 12);
                }

                if (chance >= 131 && chance <= 200) {
                    // 64 bottle o enchanting uncommon
                    givePrize(cPlayer, location, 13);
                }

                if (chance >= 211 && chance <= 290) {
                    // 6 gold apples uncommon
                    givePrize(cPlayer, location, 14);
                }

                if (chance >= 301 && chance <= 390) {
                    // 1 potion of jump boost III 1 minute uncommon
                    givePrize(cPlayer, location, 15);
                }

                if (chance >= 391 && chance <= 480) {
                    // 1 potion of speed IV 1 minute uncommon
                    givePrize(cPlayer, location, 16);
                }

                if (chance >= 481 && chance <= 581) {
                    // 16 apples common
                    givePrize(cPlayer, location, 17);
                }

                if (chance >= 582 && chance <= 700) {
                    // 32 steak common
                    givePrize(cPlayer, location, 18);
                }

                if (chance >= 701 && chance <= 800) {
                    // 32 pork common
                    givePrize(cPlayer, location, 19);
                }

                if (chance >= 801 && chance <= 900) {
                    // 32 chicken common
                    givePrize(cPlayer, location, 20);
                }

                if (chance >= 901 && chance <= 1000) {
                    // 64 potato common
                    givePrize(cPlayer, location, 21);
                }
            }

            if (odds >= 55 && odds <= 100) {
                // money
                int chance = rand.nextInt(1000) + 1;

                if (chance >= 701 && chance <= 1000) {
                    // 50 common
                    givePrize(cPlayer, location, 22);
                }

                if (chance >= 401 && chance <= 700) {
                    // 100 common
                    givePrize(cPlayer, location, 23);
                }

                if (chance >= 251 && chance <= 400) {
                    // 250 uncommon
                    givePrize(cPlayer, location, 24);
                }

                if (chance >= 101 && chance <= 250) {
                    // 500 uncommon
                    givePrize(cPlayer, location, 25);
                }

                if (chance >= 46 && chance <= 100) {
                    // 1000 rare
                    givePrize(cPlayer, location, 26);
                }

                if (chance >= 21 && chance <= 45) {
                    // 1500 rare
                    givePrize(cPlayer, location, 27);
                }

                if (chance >= 8 && chance <= 20) {
                    // 2000 rare
                    givePrize(cPlayer, location, 28);
                }

                if (chance <= 7) {
                    // 5000 legendary
                    givePrize(cPlayer, location, 29);
                }
            }

            return true;
        }

        return false;
    }

    // loc is the flare location, not the player location.
    public static void givePrize(CorePlayer cPlayer, Location loc, int prize) {

        if (prize == 1) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.MENDING, 1, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §cLEGENDARY§7 Mending book!");
        }

        if (prize == 2) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.DURABILITY, 3, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 Unbreaking III book!");
        }

        if (prize == 3) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.LOOT_BONUS_BLOCKS, 2, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 Fortune II book!");
        }

        if (prize == 4) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 Protection II book!");
        }

        if (prize == 5) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.DIG_SPEED, 2, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 Efficiency II book!");
        }

        if (prize == 6) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.DAMAGE_ALL, 2, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a COMMON Sharp II book!");
        }

        if (prize == 7) {
            ItemStack i = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta m = (EnchantmentStorageMeta) i.getItemMeta();
            m.addStoredEnchant(Enchantment.ARROW_DAMAGE, 2, false);
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a COMMON Power II book!");
        }

        if (prize == 8) {
            ItemStack i = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §cLEGENDARY§7 Notch apple!");
        }

        if (prize == 9) {
            ItemStack i = new ItemStack(Material.NETHER_STAR);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §cLEGENDARY§7 Nether star!");
        }

        if (prize == 10) {
            ItemStack i = new ItemStack(Material.ELYTRA);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 elytra!");
        }

        if (prize == 11) {
            ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 wither's skull!");
        }

        if (prize == 12) {
            ItemStack i = new ItemStack(Material.DRAGONS_BREATH, 4);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned four §6RARE§7 dragon's breath!");
        }

        if (prize == 13) {
            ItemStack i = new ItemStack(Material.EXP_BOTTLE, 64);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned sixty four §bUNCOMMON§7 experience bottles!");
        }

        if (prize == 14) {
            ItemStack i = new ItemStack(Material.GOLDEN_APPLE, 6);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned six §bUNCOMMON§7 golden apples!");
        }

        if (prize == 15) {
            ItemStack i = new ItemStack(Material.POTION);
            PotionMeta m = (PotionMeta) i.getItemMeta();
            m.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, (60*8)*20, 3, false, true), true);
            m.setDisplayName("§fPotion of Leaping §b[Special]");
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 jump boost IV potion!");
        }

        if (prize == 16) {
            ItemStack i = new ItemStack(Material.POTION);
            PotionMeta m = (PotionMeta) i.getItemMeta();
            m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 90*20, 4, false, true), true);
            m.setDisplayName("§fPotion of Swiftness §b[Special]");
            i.setItemMeta(m);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 speed V potion!");
        }

        if (prize == 17) {
            ItemStack i = new ItemStack(Material.APPLE, 16);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned sixteen COMMON apples!");
        }

        if (prize == 18) {
            ItemStack i = new ItemStack(Material.COOKED_BEEF, 32);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned thirty-two COMMON steaks!");
        }

        if (prize == 19) {
            ItemStack i = new ItemStack(Material.GRILLED_PORK, 32);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned thirty-two COMMON porkchops!");
        }

        if (prize == 20) {
            ItemStack i = new ItemStack(Material.COOKED_CHICKEN, 32);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned thirty-two COMMON cooked chickens!");
        }

        if (prize == 21) {
            ItemStack i = new ItemStack(Material.POTATO_ITEM, 64);
            giveItem(cPlayer.getPlayer(), loc, i);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned sixty-four COMMON raw potatoes!");
        }

        if (prize == 22) {
            giveMoney(cPlayer.getPlayer(), 50);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a COMMON 50Ƶ!");
        }

        if (prize == 23) {
            giveMoney(cPlayer.getPlayer(), 100);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a COMMON 100Ƶ!");
        }

        if (prize == 24) {
            giveMoney(cPlayer.getPlayer(), 250);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 250Ƶ!");
        }

        if (prize == 25) {
            giveMoney(cPlayer.getPlayer(), 500);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned an §bUNCOMMON§7 500Ƶ!");
        }

        if (prize == 26) {
            giveMoney(cPlayer.getPlayer(), 1000);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 1000Ƶ!");
        }

        if (prize == 27) {
            giveMoney(cPlayer.getPlayer(), 1500);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 1500Ƶ!");
        }

        if (prize == 28) {
            giveMoney(cPlayer.getPlayer(), 2000);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §6RARE§7 2000Ƶ!");
        }

        if (prize == 29) {
            giveMoney(cPlayer.getPlayer(), 5000);
            Bukkit.broadcastMessage(cPlayer.getColorPrefix() + cPlayer.getNick() + "§7 earned a §cLEGENDARY§7 5000Ƶ!");
        }
    }

    public static void giveItem(Player p, Location loc, ItemStack i) {
        if (p.getInventory().firstEmpty() != -1) {
            // They have room in their inventory.
            p.getInventory().addItem(i);
        } else {
            // They do not, drop it on the ground instead. loc is the flare location.
            dropItem(loc, i);
        }
    }

    public static void dropItem(Location loc, ItemStack i) {
        loc.getWorld().dropItem(loc, i);
    }

    public static void giveMoney(Player p, double amount) {
        Vault.economy.depositPlayer(p, amount);
    }
}