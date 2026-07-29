package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.gui.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener implements Listener {

    private final SpazioDuelsPlugin plugin;

    public GUIListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof KitSelectorGUI gui) {
            event.setCancelled(true);
            gui.handleCLick(player, event.getCurrentItem());
        } else if (holder instanceof AdminKitsGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getCurrentItem());
        } else if (holder instanceof KothLootGUI gui) {
            int rawSlot = event.getRawSlot();
            // Cancel clicks in bottom control bar (slots 45-53)
            if (rawSlot >= 45 && rawSlot < 54) {
                event.setCancelled(true);
                gui.handleClick(player, rawSlot);
            }
        } else if (holder instanceof EventSummaryGUI || holder instanceof ArenaAdminGUI) {
            event.setCancelled(true);
        }
    }
}
