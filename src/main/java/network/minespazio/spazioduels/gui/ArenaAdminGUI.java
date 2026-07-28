package network.minespazio.spazioduels.gui;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.util.ItemBuilder;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collection;

public class ArenaAdminGUI implements InventoryHolder {

    public static final String TITLE = TextUtil.colorize("&8Panel de Arenas - Setup");
    private final SpazioDuelsPlugin plugin;
    private Inventory inventory;

    public ArenaAdminGUI(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Collection<Arena> arenas = plugin.getArenaManager().getArenas();
        this.inventory = Bukkit.createInventory(this, 54, TITLE);

        int slot = 0;
        for (Arena arena : arenas) {
            Material mat = arena.isReady() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
            ItemBuilder builder = new ItemBuilder(mat)
                    .name("&eArena: &b" + arena.getName())
                    .lore(
                            "&7Estado: &f" + arena.getState().name(),
                            "&7Spawn 1: " + (arena.getSpawn1() != null ? "&aConfigurado" : "&cNo configurado"),
                            "&7Spawn 2: " + (arena.getSpawn2() != null ? "&aConfigurado" : "&cNo configurado"),
                            "&7Espectador: " + (arena.getSpectatorSpawn() != null ? "&aConfigurado" : "&cNo configurado"),
                            "",
                            "&eComandos de Setup:",
                            "&7/sd setup setspawn1 " + arena.getName(),
                            "&7/sd setup setspawn2 " + arena.getName(),
                            "&7/sd setup setspectator " + arena.getName()
                    );
            this.inventory.setItem(slot++, builder.build());
        }

        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
