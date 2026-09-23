/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 */
package network.minespazio.spazioduels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.colorize(name));
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(String ... lore) {
        return this.lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.setLore(TextUtil.colorize(lore));
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<String>();
            }
            lore.add(TextUtil.colorize(line));
            meta.setLore(lore);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder flag(ItemFlag ... flags) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder pdcString(NamespacedKey key, String value) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack build() {
        return this.item;
    }
}

