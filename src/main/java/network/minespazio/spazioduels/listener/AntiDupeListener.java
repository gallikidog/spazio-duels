package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiDupeListener implements Listener {

    private final SpazioDuelsPlugin plugin;

    public AntiDupeListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player has an unrestored inventory backup (due to disconnect during duel or server reboot)
        if (plugin.getInventoryBackupManager().hasBackup(player)) {
            boolean restored = plugin.getInventoryBackupManager().restoreBackup(player);
            if (restored) {
                player.sendMessage(TextUtil.colorize("&a[SpazioDuels] Tu inventario original ha sido restaurado exitosamente."));
                plugin.getLogger().info("Inventario respaldado restaurado para " + player.getName() + " al conectarse.");
            }
        }
    }
}
