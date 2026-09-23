/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.NamespacedKey
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 */
package network.minespazio.spazioduels.antidupe;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AntiDupeManager {
    private final SpazioDuelsPlugin plugin;
    private final NamespacedKey kitItemKey;

    public AntiDupeManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kitItemKey = new NamespacedKey((Plugin)plugin, "kit_item");
    }

    public NamespacedKey getKitItemKey() {
        return this.kitItemKey;
    }

    public ItemStack markKitItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return item;
        }
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(this.kitItemKey, PersistentDataType.STRING, "true");
            clone.setItemMeta(meta);
        }
        return clone;
    }

    public ItemStack[] markKitItems(ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }
        ItemStack[] marked = new ItemStack[items.length];
        for (int i = 0; i < items.length; ++i) {
            marked[i] = this.markKitItem(items[i]);
        }
        return marked;
    }

    public boolean isKitItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(this.kitItemKey, PersistentDataType.STRING);
    }

    public void purgeKitItems(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; ++i) {
            if (!this.isKitItem(contents[i])) continue;
            contents[i] = null;
        }
        player.getInventory().setContents(contents);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; ++i) {
            if (!this.isKitItem(armor[i])) continue;
            armor[i] = null;
        }
        player.getInventory().setArmorContents(armor);
        if (this.isKitItem(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    public void markPlayerInventory(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; ++i) {
            if (contents[i] == null || contents[i].getType().isAir()) continue;
            contents[i] = this.markKitItem(contents[i]);
        }
        player.getInventory().setStorageContents(contents);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; ++i) {
            if (armor[i] == null || armor[i].getType().isAir()) continue;
            armor[i] = this.markKitItem(armor[i]);
        }
        player.getInventory().setArmorContents(armor);
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            player.getInventory().setItemInOffHand(this.markKitItem(offhand));
        }
    }
}

