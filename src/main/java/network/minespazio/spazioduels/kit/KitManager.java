package network.minespazio.spazioduels.kit;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class KitManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private File file;
    private YamlConfiguration config;

    public KitManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        initFile();
        loadKits();
    }

    private void initFile() {
        file = new File(plugin.getDataFolder(), "kits.yml");
        if (!file.exists()) {
            plugin.saveResource("kits.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadKits() {
        kits.clear();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        for (String kitName : section.getKeys(false)) {
            try {
                ConfigurationSection kitSec = section.getConfigurationSection(kitName);
                if (kitSec == null) continue;

                ItemStack icon = kitSec.getItemStack("icon");
                if (icon == null) icon = new ItemStack(Material.DIAMOND_SWORD);

                List<?> contentsList = kitSec.getList("contents");
                ItemStack[] contents = contentsList != null ? contentsList.toArray(new ItemStack[0]) : new ItemStack[36];

                List<?> armorList = kitSec.getList("armor");
                ItemStack[] armor = armorList != null ? armorList.toArray(new ItemStack[0]) : new ItemStack[4];

                ItemStack offHand = kitSec.getItemStack("offhand");

                List<?> potionList = kitSec.getList("potion_effects");
                List<PotionEffect> effects = new ArrayList<>();
                if (potionList != null) {
                    for (Object obj : potionList) {
                        if (obj instanceof PotionEffect effect) {
                            effects.add(effect);
                        }
                    }
                }

                boolean allowBuilding = kitSec.getBoolean("allow_building", false);

                Kit kit = new Kit(kitName, icon, contents, armor, offHand, effects, allowBuilding);
                kits.put(kitName.toLowerCase(), kit);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el kit: " + kitName, e);
            }
        }
        plugin.getLogger().info("Se cargaron " + kits.size() + " kits de duelos.");
    }

    public void saveKits() {
        config.set("kits", null);
        for (Kit kit : kits.values()) {
            String path = "kits." + kit.getName();
            config.set(path + ".icon", kit.getIcon());
            config.set(path + ".contents", kit.getContents());
            config.set(path + ".armor", kit.getArmor());
            config.set(path + ".offhand", kit.getOffHand());
            config.set(path + ".potion_effects", kit.getPotionEffects());
            config.set(path + ".allow_building", kit.isAllowBuilding());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar kits.yml", e);
        }
    }

    public Kit createKitFromPlayer(String name, Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        Collection<PotionEffect> effects = player.getActivePotionEffects();

        ItemStack icon = player.getInventory().getItemInMainHand();
        if (icon == null || icon.getType() == Material.AIR) {
            icon = new ItemStack(Material.DIAMOND_SWORD);
        } else {
            icon = icon.clone();
        }

        Kit kit = new Kit(name, icon, contents, armor, offHand, effects, false);
        kits.put(name.toLowerCase(), kit);
        saveKits();
        return kit;
    }

    public Kit getKit(String name) {
        if (name == null) return null;
        return kits.get(name.toLowerCase());
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }

    public boolean deleteKit(String name) {
        if (kits.remove(name.toLowerCase()) != null) {
            saveKits();
            return true;
        }
        return false;
    }
}
