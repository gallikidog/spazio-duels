/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerPickupItemEvent
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.listener;

import java.util.List;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectionListener
implements Listener {
    private final SpazioDuelsPlugin plugin;

    public ProtectionListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getDuelManager().isInDuel(player)) {
            return;
        }
        if (player.hasPermission("spazioduels.admin.bypass")) {
            return;
        }
        String cmd = event.getMessage().toLowerCase();
        List<String> whitelist = this.plugin.getConfig().getStringList("protection.command_whitelist");
        boolean allowed = false;
        if (whitelist != null) {
            for (String allowedCmd : whitelist) {
                if (!cmd.startsWith(allowedCmd.toLowerCase())) continue;
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes ejecutar comandos durante un duelo. Usa &e/duel forfeit &cpara rendirte."));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        if (this.plugin.getAntiDupeManager().isKitItem(item) || this.plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes tirar objetos del kit durante el duelo."));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onContainerClick(InventoryClickEvent event) {
        InventoryType topType;
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        boolean isKitItem = this.plugin.getAntiDupeManager().isKitItem(current) || this.plugin.getAntiDupeManager().isKitItem(cursor);
        boolean bl = isKitItem;
        if ((isKitItem || this.plugin.getDuelManager().isInDuel(player)) && (topType = event.getView().getTopInventory().getType()) != InventoryType.CRAFTING && topType != InventoryType.PLAYER) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes guardar objetos de duelos en contenedores o cofres."));
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = this.plugin.getDuelManager().getMatch(player);
        if (match != null) {
            if (match.getKit() != null && match.getKit().isAllowBuilding()) {
                match.getArena().getRollback().trackBlockPlace(event.getBlock());
            } else {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&cLa construcci\u00f3n de bloques est\u00e1 desactivada en este kit."));
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = this.plugin.getDuelManager().getMatch(player);
        if (match != null && !match.getKit().isAllowBuilding()) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes romper bloques durante el duelo."));
        }
    }
}

