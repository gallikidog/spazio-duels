package network.minespazio.spazioduels.antidupe;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class InventoryBackupManager {

    private final SpazioDuelsPlugin plugin;
    private final File backupDir;

    public InventoryBackupManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "data" + File.separator + "inventories");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    public void saveBackup(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        File file = new File(backupDir, uuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("uuid", uuid.toString());
        config.set("name", player.getName());
        config.set("contents", player.getInventory().getStorageContents());
        config.set("armor", player.getInventory().getArmorContents());
        config.set("offhand", player.getInventory().getItemInOffHand());
        config.set("effects", player.getActivePotionEffects());
        config.set("health", player.getHealth());
        config.set("food", player.getFoodLevel());
        config.set("exp", player.getExp());
        config.set("level", player.getLevel());
        config.set("gamemode", player.getGameMode().name());
        config.set("allow_flight", player.getAllowFlight());
        config.set("flying", player.isFlying());

        Location loc = player.getLocation();
        if (loc != null && loc.getWorld() != null) {
            config.set("location.world", loc.getWorld().getName());
            config.set("location.x", loc.getX());
            config.set("location.y", loc.getY());
            config.set("location.z", loc.getZ());
            config.set("location.yaw", loc.getYaw());
            config.set("location.pitch", loc.getPitch());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al guardar el respaldo de inventario para " + player.getName(), e);
        }
    }

    public boolean restoreBackup(Player player) {
        if (player == null) return false;
        UUID uuid = player.getUniqueId();
        File file = new File(backupDir, uuid.toString() + ".yml");
        if (!file.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        try {
            // Clear current inventory and active potion effects
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            List<?> contentsList = config.getList("contents");
            if (contentsList != null) {
                player.getInventory().setStorageContents(contentsList.toArray(new ItemStack[0]));
            }

            List<?> armorList = config.getList("armor");
            if (armorList != null) {
                player.getInventory().setArmorContents(armorList.toArray(new ItemStack[0]));
            }

            ItemStack offhand = config.getItemStack("offhand");
            if (offhand != null) {
                player.getInventory().setItemInOffHand(offhand);
            }

            List<?> effectsList = config.getList("effects");
            if (effectsList != null) {
                for (Object obj : effectsList) {
                    if (obj instanceof PotionEffect effect) {
                        player.addPotionEffect(effect);
                    }
                }
            }

            double health = config.getDouble("health", 20.0);
            player.setHealth(Math.min(health, player.getMaxHealth()));

            player.setFoodLevel(config.getInt("food", 20));
            player.setExp((float) config.getDouble("exp", 0.0));
            player.setLevel(config.getInt("level", 0));

            String gm = config.getString("gamemode", "SURVIVAL");
            try {
                player.setGameMode(GameMode.valueOf(gm));
            } catch (Exception ignored) {}

            player.setAllowFlight(config.getBoolean("allow_flight", false));
            player.setFlying(config.getBoolean("flying", false));

            file.delete(); // Remove backup once successfully restored
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error al restaurar respaldo para " + player.getName(), e);
            return false;
        }
    }

    public boolean hasBackup(Player player) {
        if (player == null) return false;
        File file = new File(backupDir, player.getUniqueId().toString() + ".yml");
        return file.exists();
    }
}
