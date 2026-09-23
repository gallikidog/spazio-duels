/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 */
package network.minespazio.spazioduels.antidupe;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class InventoryBackupManager {
    private final SpazioDuelsPlugin plugin;
    private final File backupDir;

    public InventoryBackupManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "data" + File.separator + "inventories");
        if (!this.backupDir.exists()) {
            this.backupDir.mkdirs();
        }
    }

    public void saveBackup(Player player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        File file = new File(this.backupDir, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("uuid", (Object)uuid.toString());
        config.set("name", (Object)player.getName());
        config.set("contents", (Object)player.getInventory().getStorageContents());
        config.set("armor", (Object)player.getInventory().getArmorContents());
        config.set("offhand", (Object)player.getInventory().getItemInOffHand());
        config.set("effects", (Object)player.getActivePotionEffects());
        config.set("health", (Object)player.getHealth());
        config.set("food", (Object)player.getFoodLevel());
        config.set("exp", (Object)Float.valueOf(player.getExp()));
        config.set("level", (Object)player.getLevel());
        config.set("gamemode", (Object)player.getGameMode().name());
        config.set("allow_flight", (Object)player.getAllowFlight());
        config.set("flying", (Object)player.isFlying());
        Location loc = player.getLocation();
        if (loc != null && loc.getWorld() != null) {
            config.set("location.world", (Object)loc.getWorld().getName());
            config.set("location.x", (Object)loc.getX());
            config.set("location.y", (Object)loc.getY());
            config.set("location.z", (Object)loc.getZ());
            config.set("location.yaw", (Object)Float.valueOf(loc.getYaw()));
            config.set("location.pitch", (Object)Float.valueOf(loc.getPitch()));
        }
        try {
            config.save(file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Error al guardar el respaldo de inventario para " + player.getName(), e);
        }
    }

    public boolean restoreBackup(Player player) {
        if (player == null) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        File file = new File(this.backupDir, uuid.toString() + ".yml");
        if (!file.exists()) {
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
        try {
            List effectsList;
            ItemStack offhand;
            List armorList;
            ItemStack is;
            Object itemObj;
            int i;
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            List contentsList = config.getList("contents");
            if (contentsList != null) {
                ItemStack[] contents = new ItemStack[36];
                for (i = 0; i < Math.min(contentsList.size(), 36); ++i) {
                    itemObj = contentsList.get(i);
                    if (!(itemObj instanceof ItemStack)) continue;
                    contents[i] = is = (ItemStack)itemObj;
                }
                player.getInventory().setStorageContents(contents);
            }
            if ((armorList = config.getList("armor")) != null) {
                ItemStack[] armor = new ItemStack[4];
                for (i = 0; i < Math.min(armorList.size(), 4); ++i) {
                    itemObj = armorList.get(i);
                    if (!(itemObj instanceof ItemStack)) continue;
                    is = (ItemStack)itemObj;
                    armor[i] = is;
                }
                player.getInventory().setArmorContents(armor);
            }
            if ((offhand = config.getItemStack("offhand")) != null) {
                player.getInventory().setItemInOffHand(offhand);
            }
            if ((effectsList = config.getList("effects")) != null) {
                for (Object obj : effectsList) {
                    if (obj instanceof PotionEffect) {
                        PotionEffect effect = (PotionEffect)obj;
                        player.addPotionEffect(effect);
                        continue;
                    }
                    if (!(obj instanceof Map)) continue;
                    Map map = (Map)obj;
                    try {
                        Map castMap = map;
                        player.addPotionEffect(new PotionEffect(castMap));
                    }
                    catch (Exception exception) {}
                }
            }
            double health = config.getDouble("health", 20.0);
            player.setHealth(Math.min(health, player.getMaxHealth()));
            player.setFoodLevel(config.getInt("food", 20));
            player.setExp((float)config.getDouble("exp", 0.0));
            player.setLevel(config.getInt("level", 0));
            String gm = config.getString("gamemode", "SURVIVAL");
            try {
                player.setGameMode(GameMode.valueOf((String)gm));
            }
            catch (Exception exception) {
                // empty catch block
            }
            player.setAllowFlight(config.getBoolean("allow_flight", false));
            player.setFlying(config.getBoolean("flying", false));
            file.delete();
            return true;
        }
        catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Error al restaurar respaldo para " + player.getName(), e);
            return false;
        }
    }

    public boolean hasBackup(Player player) {
        if (player == null) {
            return false;
        }
        File file = new File(this.backupDir, player.getUniqueId().toString() + ".yml");
        return file.exists();
    }

    public Location getSavedLocation(Player player) {
        String wName;
        World w;
        if (player == null) {
            return null;
        }
        File file = new File(this.backupDir, player.getUniqueId().toString() + ".yml");
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)file);
        if (config.contains("location.world") && (w = Bukkit.getWorld((String)(wName = config.getString("location.world")))) != null) {
            double x = config.getDouble("location.x");
            double y = config.getDouble("location.y");
            double z = config.getDouble("location.z");
            float yaw = (float)config.getDouble("location.yaw");
            float pitch = (float)config.getDouble("location.pitch");
            return new Location(w, x, y, z, yaw, pitch);
        }
        return null;
    }
}

