/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 */
package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiDupeListener
implements Listener {
    private final SpazioDuelsPlugin plugin;

    public AntiDupeListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean restored;
        Player player = event.getPlayer();
        if (this.plugin.getInventoryBackupManager().hasBackup(player) && (restored = this.plugin.getInventoryBackupManager().restoreBackup(player))) {
            player.sendMessage(TextUtil.colorize("&a[SpazioDuels] Tu inventario original ha sido restaurado exitosamente."));
            this.plugin.getLogger().info("Inventario respaldado restaurado para " + player.getName() + " al conectarse.");
        }
    }
}

