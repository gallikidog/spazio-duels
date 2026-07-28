package network.minespazio.spazioduels.gui;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMode;
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

import java.util.List;

public class KitSelectorGUI {

    public static final String TITLE = TextUtil.colorize("&8Selecciona un Kit de Duelo");
    private final SpazioDuelsPlugin plugin;
    private final Player sender;
    private final Player target;
    private final NamespacedKey kitKey;

    public KitSelectorGUI(SpazioDuelsPlugin plugin, Player sender, Player target) {
        this.plugin = plugin;
        this.sender = sender;
        this.target = target;
        this.kitKey = new NamespacedKey(plugin, "gui_kit_name");
    }

    public void open() {
        List<Kit> kits = plugin.getKitManager().getEnabledKitsForDuels();
        int size = Math.max(27, ((kits.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, Math.min(54, size), TITLE);

        int slot = 0;
        for (Kit kit : kits) {
            ItemStack icon = kit.getIcon() != null ? kit.getIcon().clone() : new ItemStack(Material.DIAMOND_SWORD);
            ItemBuilder builder = new ItemBuilder(icon)
                    .name("&a&lKit: &e" + kit.getName())
                    .lore(
                            "&7Haz clic para enviar el duelo a &b" + target.getName(),
                            "&7con el kit &e" + kit.getName() + "&7.",
                            "",
                            "&e▶ Haz clic para enviar reto"
                    )
                    .pdcString(kitKey, kit.getName());
            inv.setItem(slot++, builder.build());
        }

        sender.openInventory(inv);
    }

    public void handleCLick(Player clicker, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String kitName = item.getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
        if (kitName != null) {
            Kit kit = plugin.getKitManager().getKit(kitName);
            if (kit != null && kit.isEnabledForDuels()) {
                clicker.closeInventory();
                plugin.getDuelManager().sendDuelRequestWithKit(sender, target, kit, DuelMode.SOLO_1V1);
            }
        }
    }
}
