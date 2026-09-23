/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package network.minespazio.spazioduels.gui;

import java.util.Collection;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.ItemBuilder;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AdminKitsGUI
implements InventoryHolder {
    public static final String TITLE = TextUtil.colorize("&8Gesti\u00f3n de Kits para Duelos");
    private final SpazioDuelsPlugin plugin;
    private final NamespacedKey kitKey;
    private Inventory inventory;

    public AdminKitsGUI(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey((Plugin)plugin, "admin_kit_name");
    }

    public void open(Player player) {
        Collection<Kit> kits = this.plugin.getKitManager().getKits();
        int size = Math.max(27, (kits.size() / 9 + 1) * 9);
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)Math.min(54, size), (String)TITLE);
        int slot = 0;
        for (Kit kit : kits) {
            Material mat = kit.isEnabledForDuels() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
            String status = kit.isEnabledForDuels() ? "&a[ HABILITADO PARA DUELOS ]" : "&c[ DESHABILITADO PARA DUELOS ]";
            String actionLore = kit.isEnabledForDuels() ? "&e\u25b6 Haz clic para DESHABILITAR" : "&e\u25b6 Haz clic para HABILITAR";
            String origin = kit.isFromPlayerKits() ? "&bPlayerKits2" : "&eNativo SpazioDuels";
            ItemBuilder builder = new ItemBuilder(mat).name("&eKit: &b" + kit.getName()).lore("&7Estado: " + status, "&7Origen: " + origin, "&7Construcci\u00f3n: " + (kit.isAllowBuilding() ? "&aS\u00ed" : "&cNo"), "", actionLore).pdcString(this.kitKey, kit.getName());
            this.inventory.setItem(slot++, builder.build());
        }
        player.openInventory(this.inventory);
    }

    public void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        String kitName = (String)item.getItemMeta().getPersistentDataContainer().get(this.kitKey, PersistentDataType.STRING);
        if (kitName != null) {
            boolean newState = this.plugin.getKitManager().toggleKitEnabledForDuels(kitName);
            player.sendMessage(TextUtil.colorize("&aEl kit &b" + kitName + " &aahora est\u00e1: " + (newState ? "&aHABILITADO" : "&cDESHABILITADO") + " &apara duelos."));
            this.open(player);
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

