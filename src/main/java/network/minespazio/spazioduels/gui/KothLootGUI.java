package network.minespazio.spazioduels.gui;

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

import java.util.ArrayList;
import java.util.List;

public class KothLootGUI implements InventoryHolder {

    public static final String TITLE_PREFIX = TextUtil.colorize("&8Loot KOTH: ");
    private final SpazioDuelsPlugin plugin;
    private final Koth koth;
    private Inventory inventory;

    public KothLootGUI(SpazioDuelsPlugin plugin, Koth koth) {
        this.plugin = plugin;
        this.koth = koth;
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, TITLE_PREFIX + koth.getName());

        // Load existing loot items into slots 0-44
        if (koth.getLootItems() != null) {
            int slot = 0;
            for (ItemStack item : koth.getLootItems()) {
                if (slot >= 45) break;
                if (item != null && !item.getType().isAir()) {
                    this.inventory.setItem(slot++, item.clone());
                }
            }
        }

        // Fill bottom control bar (row 6: slots 45-53)
        ItemBuilder glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int i = 45; i < 54; i++) {
            if (i != 49) {
                this.inventory.setItem(i, glass.build());
            }
        }

        // Slot 49: SAVE LOOT button
        ItemBuilder saveButton = new ItemBuilder(Material.EMERALD_BLOCK)
                .name("&a&l[ GUARDAR LOOT ]")
                .lore(
                        "&7Haz clic aquí para guardar todos los",
                        "&7ítems colocados arriba como botín",
                        "&7del KOTH &b" + koth.getName() + "&7.",
                        "",
                        "&e▶ Haz clic para guardar"
                );
        this.inventory.setItem(49, saveButton.build());

        player.openInventory(this.inventory);
    }

    public void handleClick(Player player, int rawSlot) {
        // If clicking bottom control bar (slots 45-53)
        if (rawSlot >= 45 && rawSlot < 54) {
            if (rawSlot == 49) {
                // Save Loot
                saveLoot(player);
            }
        }
    }

    private void saveLoot(Player player) {
        List<ItemStack> newLoot = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                newLoot.add(item.clone());
            }
        }

        koth.setLootItems(newLoot);
        plugin.getKothManager().saveKoths();

        player.closeInventory();
        player.sendMessage(TextUtil.colorize("&a¡Botín del KOTH &b" + koth.getName() + " &aguardado exitosamente! (" + newLoot.size() + " ítems)"));
    }

    public Koth getKoth() {
        return koth;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
