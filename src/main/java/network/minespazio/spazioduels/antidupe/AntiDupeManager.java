package network.minespazio.spazioduels.antidupe;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AntiDupeManager {

    private final SpazioDuelsPlugin plugin;
    private final NamespacedKey kitItemKey;

    public AntiDupeManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kitItemKey = new NamespacedKey(plugin, "kit_item");
    }

    public NamespacedKey getKitItemKey() {
        return kitItemKey;
    }

    public ItemStack markKitItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(kitItemKey, PersistentDataType.STRING, "true");
            clone.setItemMeta(meta);
        }
        return clone;
    }

    public ItemStack[] markKitItems(ItemStack[] items) {
        if (items == null) return new ItemStack[0];
        ItemStack[] marked = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            marked[i] = markKitItem(items[i]);
        }
        return marked;
    }

    public boolean isKitItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(kitItemKey, PersistentDataType.STRING);
    }

    public void purgeKitItems(Player player) {
        if (player == null) return;
        
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (isKitItem(contents[i])) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (isKitItem(armor[i])) {
                armor[i] = null;
            }
        }
        player.getInventory().setArmorContents(armor);

        if (isKitItem(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }
}
