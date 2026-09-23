/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.gui;

import java.util.ArrayList;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.util.ItemBuilder;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class KothLootGUI
implements InventoryHolder {
    public static final String TITLE_PREFIX = TextUtil.colorize("&8Loot KOTH: ");
    private final SpazioDuelsPlugin plugin;
    private final Koth koth;
    private Inventory inventory;

    public KothLootGUI(SpazioDuelsPlugin plugin, Koth koth) {
        this.plugin = plugin;
        this.koth = koth;
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)54, (String)(TITLE_PREFIX + this.koth.getName()));
        if (this.koth.getLootItems() != null) {
            int slot = 0;
            for (ItemStack item : this.koth.getLootItems()) {
                if (slot >= 45) break;
                if (item == null || item.getType().isAir()) continue;
                this.inventory.setItem(slot++, item.clone());
            }
        }
        ItemBuilder glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int i = 45; i < 54; ++i) {
            if (i == 49) continue;
            this.inventory.setItem(i, glass.build());
        }
        ItemBuilder saveButton = new ItemBuilder(Material.EMERALD_BLOCK).name("&a&l[ GUARDAR LOOT ]").lore("&7Haz clic aqu\u00ed para guardar todos los", "&7\u00edtems colocados arriba como bot\u00edn", "&7del KOTH &b" + this.koth.getName() + "&7.", "", "&e\u25b6 Haz clic para guardar");
        this.inventory.setItem(49, saveButton.build());
        player.openInventory(this.inventory);
    }

    public void handleClick(Player player, int rawSlot) {
        if (rawSlot >= 45 && rawSlot < 54 && rawSlot == 49) {
            this.saveLoot(player);
        }
    }

    private void saveLoot(Player player) {
        ArrayList<ItemStack> newLoot = new ArrayList<ItemStack>();
        for (int i = 0; i < 45; ++i) {
            ItemStack item = this.inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            newLoot.add(item.clone());
        }
        this.koth.setLootItems(newLoot);
        this.plugin.getKothManager().saveKoths();
        player.closeInventory();
        player.sendMessage(TextUtil.colorize("&a\u00a1Bot\u00edn del KOTH &b" + this.koth.getName() + " &aguardado exitosamente! (" + newLoot.size() + " \u00edtems)"));
    }

    public Koth getKoth() {
        return this.koth;
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

