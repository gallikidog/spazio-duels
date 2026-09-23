/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionType
 */
package network.minespazio.spazioduels.kit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.integration.PlayerKitsHook;
import network.minespazio.spazioduels.kit.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class KitManager {
    private final SpazioDuelsPlugin plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<String, Kit>();
    private PlayerKitsHook playerKitsHook;
    private File file;
    private YamlConfiguration config;

    public KitManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.playerKitsHook = new PlayerKitsHook(plugin);
        this.initFile();
        this.loadKits();
    }

    private void initFile() {
        this.file = new File(this.plugin.getDataFolder(), "kits.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("kits.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void loadKits() {
        this.kits.clear();
        ConfigurationSection section = this.config.getConfigurationSection("kits");
        if (section != null) {
            for (String kitName : section.getKeys(false)) {
                try {
                    ConfigurationSection kitSec = section.getConfigurationSection(kitName);
                    if (kitSec == null) continue;
                    ItemStack icon = kitSec.getItemStack("icon");
                    if (icon == null) {
                        icon = new ItemStack(Material.DIAMOND_SWORD);
                    }
                    List contentsList = kitSec.getList("contents");
                    ItemStack[] contents = new ItemStack[36];
                    if (contentsList != null) {
                        for (int i = 0; i < Math.min(contentsList.size(), 36); ++i) {
                            ItemStack is;
                            Object itemObj = contentsList.get(i);
                            if (!(itemObj instanceof ItemStack)) continue;
                            contents[i] = is = (ItemStack)itemObj;
                        }
                    }
                    List armorList = kitSec.getList("armor");
                    ItemStack[] armor = new ItemStack[4];
                    if (armorList != null) {
                        for (int i = 0; i < Math.min(armorList.size(), 4); ++i) {
                            ItemStack is;
                            Object itemObj = armorList.get(i);
                            if (!(itemObj instanceof ItemStack)) continue;
                            armor[i] = is = (ItemStack)itemObj;
                        }
                    }
                    ItemStack offHand = kitSec.getItemStack("offhand");
                    List potionList = kitSec.getList("potion_effects");
                    ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
                    if (potionList != null) {
                        for (Object obj : potionList) {
                            if (obj instanceof PotionEffect) {
                                PotionEffect effect = (PotionEffect)obj;
                                effects.add(effect);
                                continue;
                            }
                            if (!(obj instanceof Map)) continue;
                            Map map = (Map)obj;
                            try {
                                Map castMap = map;
                                effects.add(new PotionEffect(castMap));
                            }
                            catch (Exception castMap) {}
                        }
                    }
                    boolean allowBuilding = kitSec.getBoolean("allow_building", false);
                    boolean enabledForDuels = kitSec.getBoolean("enabled_for_duels", true);
                    boolean fromPlayerKits = kitSec.getBoolean("from_player_kits", false);
                    Kit kit = new Kit(kitName, icon, contents, armor, offHand, effects, allowBuilding, enabledForDuels, fromPlayerKits);
                    this.kits.put(kitName.toLowerCase(), kit);
                }
                catch (Exception e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error al cargar el kit: " + kitName, e);
                }
            }
        }
        if (this.playerKitsHook != null && this.playerKitsHook.isEnabled()) {
            List<Kit> pkKits = this.playerKitsHook.importKitsFromPlayerKits();
            for (Kit pkKit : pkKits) {
                String key = pkKit.getName().toLowerCase();
                Kit existing = this.kits.get(key);
                if (existing != null) {
                    pkKit.setAllowBuilding(existing.isAllowBuilding());
                    pkKit.setEnabledForDuels(existing.isEnabledForDuels());
                } else if (this.config.contains("kits." + pkKit.getName() + ".enabled_for_duels")) {
                    pkKit.setEnabledForDuels(this.config.getBoolean("kits." + pkKit.getName() + ".enabled_for_duels"));
                }
                this.kits.put(key, pkKit);
            }
        }
        this.ensureDefaultPracticeKits();
        this.plugin.getLogger().info("Se cargaron/sincronizaron un total de " + this.kits.size() + " kits de duelos.");
    }

    private void ensureDefaultPracticeKits() {
        boolean changed = false;
        changed |= this.addDefaultPracticeKit(this.createNoDebuffKit());
        changed |= this.addDefaultPracticeKit(this.createSoupKit());
        if (!(changed |= this.addDefaultPracticeKit(this.createUhcKit()))) {
            return;
        }
        try {
            this.config.save(this.file);
        }
        catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "No se pudieron guardar los kits de practica predeterminados.", exception);
        }
    }

    private boolean addDefaultPracticeKit(Kit kit) {
        String key = kit.getName().toLowerCase();
        if (this.kits.containsKey(key)) {
            return false;
        }
        this.kits.put(key, kit);
        String path = "kits." + kit.getName();
        this.config.set(path + ".icon", (Object)kit.getIcon());
        this.config.set(path + ".contents", (Object)kit.getContents());
        this.config.set(path + ".armor", (Object)kit.getArmor());
        this.config.set(path + ".offhand", (Object)kit.getOffHand());
        this.config.set(path + ".potion_effects", kit.getPotionEffects());
        this.config.set(path + ".allow_building", (Object)kit.isAllowBuilding());
        this.config.set(path + ".enabled_for_duels", (Object)true);
        this.config.set(path + ".from_player_kits", (Object)false);
        return true;
    }

    private Kit createNoDebuffKit() {
        ItemStack[] contents = new ItemStack[36];
        contents[0] = this.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 2);
        contents[1] = new ItemStack(Material.ENDER_PEARL, 16);
        for (int slot = 2; slot < contents.length; ++slot) {
            contents[slot] = this.healingPotion();
        }
        ItemStack[] armor = this.diamondArmor(2);
        return new Kit("NoDebuff", this.enchanted(Material.SPLASH_POTION, Enchantment.UNBREAKING, 1), contents, armor, null, new ArrayList<PotionEffect>(), false, true, false);
    }

    private Kit createSoupKit() {
        ItemStack[] contents = new ItemStack[36];
        contents[0] = this.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 1);
        for (int slot = 1; slot < contents.length; ++slot) {
            contents[slot] = new ItemStack(Material.MUSHROOM_STEW);
        }
        ItemStack[] armor = new ItemStack[]{this.enchanted(Material.IRON_BOOTS, Enchantment.PROTECTION, 2), this.enchanted(Material.IRON_LEGGINGS, Enchantment.PROTECTION, 2), this.enchanted(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 2), this.enchanted(Material.IRON_HELMET, Enchantment.PROTECTION, 2)};
        return new Kit("Soup", new ItemStack(Material.MUSHROOM_STEW), contents, armor, null, new ArrayList<PotionEffect>(), false, true, false);
    }

    private Kit createUhcKit() {
        ItemStack[] contents = new ItemStack[36];
        contents[0] = this.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3);
        contents[1] = this.enchanted(Material.BOW, Enchantment.POWER, 3);
        contents[2] = new ItemStack(Material.GOLDEN_APPLE, 8);
        contents[3] = new ItemStack(Material.OAK_PLANKS, 64);
        contents[4] = new ItemStack(Material.WATER_BUCKET);
        contents[5] = new ItemStack(Material.LAVA_BUCKET);
        contents[6] = new ItemStack(Material.COBWEB, 8);
        contents[7] = this.enchanted(Material.DIAMOND_PICKAXE, Enchantment.EFFICIENCY, 3);
        contents[8] = new ItemStack(Material.ARROW, 32);
        contents[9] = new ItemStack(Material.GOLDEN_APPLE, 8);
        contents[10] = new ItemStack(Material.WATER_BUCKET);
        contents[11] = new ItemStack(Material.LAVA_BUCKET);
        return new Kit("UHC", this.enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 3), contents, this.diamondArmor(2), new ItemStack(Material.SHIELD), new ArrayList<PotionEffect>(), true, true, false);
    }

    private ItemStack[] diamondArmor(int protectionLevel) {
        return new ItemStack[]{this.enchanted(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, protectionLevel), this.enchanted(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, protectionLevel), this.enchanted(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, protectionLevel), this.enchanted(Material.DIAMOND_HELMET, Enchantment.PROTECTION, protectionLevel)};
    }

    private ItemStack enchanted(Material material, Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(material);
        item.addUnsafeEnchantment(enchantment, level);
        return item;
    }

    private ItemStack healingPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta)potion.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.STRONG_HEALING);
            potion.setItemMeta((ItemMeta)meta);
        }
        return potion;
    }

    public void saveKits() {
        this.config.set("kits", null);
        for (Kit kit : this.kits.values()) {
            String path = "kits." + kit.getName();
            this.config.set(path + ".icon", (Object)kit.getIcon());
            this.config.set(path + ".contents", (Object)kit.getContents());
            this.config.set(path + ".armor", (Object)kit.getArmor());
            this.config.set(path + ".offhand", (Object)kit.getOffHand());
            this.config.set(path + ".potion_effects", kit.getPotionEffects());
            this.config.set(path + ".allow_building", (Object)kit.isAllowBuilding());
            this.config.set(path + ".enabled_for_duels", (Object)kit.isEnabledForDuels());
            this.config.set(path + ".from_player_kits", (Object)kit.isFromPlayerKits());
        }
        try {
            this.config.save(this.file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar kits.yml", e);
        }
    }

    public Kit createKitFromPlayer(String name, Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        Collection effects = player.getActivePotionEffects();
        ItemStack icon = player.getInventory().getItemInMainHand();
        icon = icon == null || icon.getType() == Material.AIR ? new ItemStack(Material.DIAMOND_SWORD) : icon.clone();
        Kit kit = new Kit(name, icon, contents, armor, offHand, effects, false, true, false);
        this.kits.put(name.toLowerCase(), kit);
        this.saveKits();
        return kit;
    }

    public Kit getKit(String name) {
        if (name == null) {
            return null;
        }
        return this.kits.get(name.toLowerCase());
    }

    public Collection<Kit> getKits() {
        return this.kits.values();
    }

    public List<Kit> getEnabledKitsForDuels() {
        ArrayList<Kit> enabledList = new ArrayList<Kit>();
        for (Kit kit : this.kits.values()) {
            if (!kit.isEnabledForDuels()) continue;
            enabledList.add(kit);
        }
        return enabledList;
    }

    public List<Kit> getDirectDuelKits() {
        String[] practiceKitNames;
        ArrayList<Kit> directKits = new ArrayList<Kit>();
        for (String name : practiceKitNames = new String[]{"NoDebuff", "Soup", "UHC"}) {
            Kit kit = this.getKit(name);
            if (kit == null) continue;
            directKits.add(kit);
        }
        return directKits;
    }

    public boolean toggleKitEnabledForDuels(String name) {
        Kit kit = this.getKit(name);
        if (kit == null) {
            return false;
        }
        kit.setEnabledForDuels(!kit.isEnabledForDuels());
        this.saveKits();
        return kit.isEnabledForDuels();
    }

    public boolean deleteKit(String name) {
        if (this.kits.remove(name.toLowerCase()) != null) {
            this.saveKits();
            return true;
        }
        return false;
    }

    public PlayerKitsHook getPlayerKitsHook() {
        return this.playerKitsHook;
    }

    public void autoEquipArmor(Player player) {
        ItemStack boots;
        ItemStack leggings;
        ItemStack chestplate;
        ItemStack helmet;
        if (player == null) {
            return;
        }
        if (this.isItemEmpty(player.getInventory().getHelmet()) && (helmet = this.findAndRemoveItem(player, "_HELMET")) != null) {
            player.getInventory().setHelmet(helmet);
        }
        if (this.isItemEmpty(player.getInventory().getChestplate()) && (chestplate = this.findAndRemoveItem(player, "_CHESTPLATE")) != null) {
            player.getInventory().setChestplate(chestplate);
        }
        if (this.isItemEmpty(player.getInventory().getLeggings()) && (leggings = this.findAndRemoveItem(player, "_LEGGINGS")) != null) {
            player.getInventory().setLeggings(leggings);
        }
        if (this.isItemEmpty(player.getInventory().getBoots()) && (boots = this.findAndRemoveItem(player, "_BOOTS")) != null) {
            player.getInventory().setBoots(boots);
        }
    }

    private boolean isItemEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }

    private ItemStack findAndRemoveItem(Player player, String suffix) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; ++i) {
            ItemStack item = contents[i];
            if (item == null || !item.getType().name().endsWith(suffix)) continue;
            contents[i] = null;
            player.getInventory().setStorageContents(contents);
            return item;
        }
        return null;
    }

    public void compactHotbar(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getStorageContents();
        if (this.isItemEmpty(contents[0])) {
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            for (int i = 0; i < contents.length; ++i) {
                if (this.isItemEmpty(contents[i])) continue;
                items.add(contents[i]);
            }
            if (!items.isEmpty()) {
                ItemStack[] newContents = new ItemStack[36];
                for (int i = 0; i < items.size() && i < 36; ++i) {
                    newContents[i] = (ItemStack)items.get(i);
                }
                player.getInventory().setStorageContents(newContents);
            }
        }
    }
}

