package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.gui.AdminKitsGUI;
import network.minespazio.spazioduels.gui.ArenaAdminGUI;
import network.minespazio.spazioduels.gui.EventSummaryGUI;
import network.minespazio.spazioduels.gui.KitSelectorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final SpazioDuelsPlugin plugin;

    public GUIListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equalsIgnoreCase(KitSelectorGUI.TITLE)) {
            event.setCancelled(true);
            KitSelectorGUI gui = new KitSelectorGUI(plugin, player, null);
            gui.handleCLick(player, event.getCurrentItem());
        } else if (title.equalsIgnoreCase(AdminKitsGUI.TITLE)) {
            event.setCancelled(true);
            AdminKitsGUI gui = new AdminKitsGUI(plugin);
            gui.handleClick(player, event.getCurrentItem());
        } else if (title.equalsIgnoreCase(EventSummaryGUI.TITLE) || title.equalsIgnoreCase(ArenaAdminGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
