/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.EquipmentSlot
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SoupPvPListener
implements Listener {
    private final SpazioDuelsPlugin plugin;

    public SoupPvPListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true)
    public void onSoupUse(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.MUSHROOM_STEW) {
            return;
        }
        Player player = event.getPlayer();
        DuelMatch match = this.plugin.getDuelManager().getMatch(player);
        if (match == null || match.getKit() == null || !match.getKit().getName().equalsIgnoreCase("Soup")) {
            return;
        }
        event.setCancelled(true);
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 7.0));
        if (event.getHand() == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.BOWL));
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.BOWL));
        }
    }
}

