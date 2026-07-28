package network.minespazio.spazioduels.gui;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.ItemBuilder;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class AdminKitsGUI {

    public static final String TITLE = TextUtil.colorize("&8Gestión de Kits para Duelos");
    private final SpazioDuelsPlugin plugin;
    private final NamespacedKey kitKey;

    public AdminKitsGUI(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kitKey = new NamespacedKey(plugin, "admin_kit_name");
    }

    public void open(Player player) {
        Collection<Kit> kits = plugin.getKitManager().getKits();
        int size = Math.max(27, ((kits.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, Math.min(54, size), TITLE);

        int slot = 0;
        for (Kit kit : kits) {
            Material mat = kit.isEnabledForDuels() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
            String status = kit.isEnabledForDuels() ? "&a[ HABILITADO PARA DUELOS ]" : "&c[ DESHABILITADO PARA DUELOS ]";
            String actionLore = kit.isEnabledForDuels() ? "&e▶ Haz clic para DESHABILITAR" : "&e▶ Haz clic para HABILITAR";
            String origin = kit.isFromPlayerKits() ? "&bPlayerKits2" : "&eNativo SpazioDuels";

            ItemBuilder builder = new ItemBuilder(mat)
                    .name("&eKit: &b" + kit.getName())
                    .lore(
                            "&7Estado: " + status,
                            "&7Origen: " + origin,
                            "&7Construcción: " + (kit.isAllowBuilding() ? "&aSí" : "&cNo"),
                            "",
                            actionLore
                    )
                    .pdcString(kitKey, kit.getName());
            inv.setItem(slot++, builder.build());
        }

        player.openInventory(inv);
    }

    public void handleClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String kitName = item.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
        if (kitName != null) {
            boolean newState = plugin.getKitManager().toggleKitEnabledForDuels(kitName);
            player.sendMessage(TextUtil.colorize("&aEl kit &b" + kitName + " &aahora está: " + (newState ? "&aHABILITADO" : "&cDESHABILITADO") + " &apara duelos."));
            open(player); // Refresh GUI
        }
    }
}
