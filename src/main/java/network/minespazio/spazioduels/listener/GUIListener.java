/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.inventory.InventoryHolder
 */
package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.gui.AdminKitsGUI;
import network.minespazio.spazioduels.gui.ArenaAdminGUI;
import network.minespazio.spazioduels.gui.EventSummaryGUI;
import network.minespazio.spazioduels.gui.KitSelectorGUI;
import network.minespazio.spazioduels.gui.KothLootGUI;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GUIListener
implements Listener {
    private final SpazioDuelsPlugin plugin;

    public GUIListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof KitSelectorGUI) {
            KitSelectorGUI gui = (KitSelectorGUI)holder;
            event.setCancelled(true);
            gui.handleCLick(player, event.getCurrentItem());
        } else if (holder instanceof AdminKitsGUI) {
            AdminKitsGUI gui = (AdminKitsGUI)holder;
            event.setCancelled(true);
            gui.handleClick(player, event.getCurrentItem());
        } else if (holder instanceof KothLootGUI) {
            KothLootGUI gui = (KothLootGUI)holder;
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 45 && rawSlot < 54) {
                event.setCancelled(true);
                gui.handleClick(player, rawSlot);
            }
        } else if (holder instanceof EventSummaryGUI || holder instanceof ArenaAdminGUI) {
            event.setCancelled(true);
        }
    }
}

