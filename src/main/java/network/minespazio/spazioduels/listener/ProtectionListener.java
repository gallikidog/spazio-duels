package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.util.TextUtil;
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

import java.util.List;

public class ProtectionListener implements Listener {

    private final SpazioDuelsPlugin plugin;

    public ProtectionListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDuelManager().isInDuel(player)) return;

        if (player.hasPermission("spazioduels.admin.bypass")) return;

        String cmd = event.getMessage().toLowerCase();
        List<String> whitelist = plugin.getConfig().getStringList("protection.command_whitelist");

        boolean allowed = false;
        if (whitelist != null) {
            for (String allowedCmd : whitelist) {
                if (cmd.startsWith(allowedCmd.toLowerCase())) {
                    allowed = true;
                    break;
                }
            }
        }

        if (!allowed) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes ejecutar comandos durante un duelo. Usa &e/duel forfeit &cpara rendirte."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (plugin.getAntiDupeManager().isKitItem(item) || plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cNo puedes tirar objetos del kit durante el duelo."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDuelManager().isInDuel(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onContainerClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean isKitItem = plugin.getAntiDupeManager().isKitItem(current) || plugin.getAntiDupeManager().isKitItem(cursor);

        if (isKitItem || plugin.getDuelManager().isInDuel(player)) {
            InventoryType topType = event.getView().getTopInventory().getType();
            if (topType != InventoryType.CRAFTING && topType != InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&cNo puedes guardar objetos de duelos en contenedores o cofres."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = plugin.getDuelManager().getMatch(player);

        if (match != null) {
            if (match.getKit() != null && match.getKit().isAllowBuilding()) {
                // Track placed block for arena rollback when match ends
                match.getArena().getRollback().trackBlockPlace(event.getBlock());
            } else {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&cLa construcción de bloques está desactivada en este kit."));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = plugin.getDuelManager().getMatch(player);

        if (match != null) {
            if (!match.getKit().isAllowBuilding()) {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&cNo puedes romper bloques durante el duelo."));
            }
        }
    }
}
