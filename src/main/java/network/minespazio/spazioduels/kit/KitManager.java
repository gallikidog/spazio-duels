package network.minespazio.spazioduels.kit;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.integration.PlayerKitsHook;
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
    private PlayerKitsHook playerKitsHook;
    private File file;
    private YamlConfiguration config;

    public KitManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.playerKitsHook = new PlayerKitsHook(plugin);
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

        // 1. Load native kits from kits.yml
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section != null) {
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
                    boolean enabledForDuels = kitSec.getBoolean("enabled_for_duels", true);

                    Kit kit = new Kit(kitName, icon, contents, armor, offHand, effects, allowBuilding, enabledForDuels, false);
                    kits.put(kitName.toLowerCase(), kit);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error al cargar el kit: " + kitName, e);
                }
            }
        }

        // 2. Import kits from PlayerKits2 if installed
        if (playerKitsHook != null && playerKitsHook.isEnabled()) {
            List<Kit> pkKits = playerKitsHook.importKitsFromPlayerKits();
            for (Kit pkKit : pkKits) {
                String key = pkKit.getName().toLowerCase();
                if (!kits.containsKey(key)) {
                    // Check if saved setting for enabled_for_duels exists in kits.yml
                    if (config.contains("kits." + pkKit.getName() + ".enabled_for_duels")) {
                        pkKit.setEnabledForDuels(config.getBoolean("kits." + pkKit.getName() + ".enabled_for_duels"));
                    }
                    kits.put(key, pkKit);
                }
            }
        }

        plugin.getLogger().info("Se cargaron/sincronizaron un total de " + kits.size() + " kits de duelos.");
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
            config.set(path + ".enabled_for_duels", kit.isEnabledForDuels());
            config.set(path + ".from_player_kits", kit.isFromPlayerKits());
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

        Kit kit = new Kit(name, icon, contents, armor, offHand, effects, false, true, false);
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

    public List<Kit> getEnabledKitsForDuels() {
        List<Kit> enabledList = new ArrayList<>();
        for (Kit kit : kits.values()) {
            if (kit.isEnabledForDuels()) {
                enabledList.add(kit);
            }
        }
        return enabledList;
    }

    public boolean toggleKitEnabledForDuels(String name) {
        Kit kit = getKit(name);
        if (kit == null) return false;
        kit.setEnabledForDuels(!kit.isEnabledForDuels());
        saveKits();
        return kit.isEnabledForDuels();
    }

    public boolean deleteKit(String name) {
        if (kits.remove(name.toLowerCase()) != null) {
            saveKits();
            return true;
        }
        return false;
    }

    public PlayerKitsHook getPlayerKitsHook() {
        return playerKitsHook;
    }
}
