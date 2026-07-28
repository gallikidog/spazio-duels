package network.minespazio.spazioduels.gui;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.event.EventSummary;
import network.minespazio.spazioduels.util.ItemBuilder;
import network.minespazio.spazioduels.util.RewardUtil;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class EventSummaryGUI implements InventoryHolder {

    public static final String TITLE = TextUtil.colorize("&8Resumen del Evento de Duelos");
    private final SpazioDuelsPlugin plugin;
    private final EventSummary summary;
    private Inventory inventory;

    public EventSummaryGUI(SpazioDuelsPlugin plugin, EventSummary summary) {
        this.plugin = plugin;
        this.summary = summary;
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, TITLE);

        // Fill background with black glass panes
        ItemBuilder glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int i = 0; i < 27; i++) {
            this.inventory.setItem(i, glass.build());
        }

        // Slot 11: Trophy / Winner Info
        ItemBuilder trophy = new ItemBuilder(Material.NETHER_STAR)
                .name("&6&l¡GANADORES DEL EVENTO!")
                .lore(
                        "&7Jugador/Equipo Ganador:",
                        "&a" + summary.getWinnerName() + " &7(" + summary.getWinnerTeamName() + ")",
                        "",
                        "&7Modo jugado: &b" + summary.getModeName(),
                        "&7Kit utilizado: &b" + summary.getKitName()
                );
        this.inventory.setItem(11, trophy.build());

        // Slot 13: Event Stats
        ItemBuilder stats = new ItemBuilder(Material.CLOCK)
                .name("&e&lESTADÍSTICAS DEL EVENTO")
                .lore(
                        "&7Nombre del Evento: &f" + summary.getEventName(),
                        "&7Duración total: &b" + summary.getDurationSeconds() + " segundos",
                        "&7Encuentros disputados: &b" + summary.getTotalMatches(),
                        "&7Eliminaciones totales: &c" + summary.getTotalKills()
                );
        this.inventory.setItem(13, stats.build());

        // Slot 15: Rewards Chest
        List<String> rewardLore = new ArrayList<>();
        rewardLore.add("&7Recompensas otorgadas:");
        for (RewardUtil.RewardItem r : summary.getRewards()) {
            rewardLore.add("&f- " + r.getName());
        }
        ItemBuilder rewardsItem = new ItemBuilder(Material.CHEST)
                .name("&a&lRECOMPENSAS ENTREGADAS")
                .lore(rewardLore);
        this.inventory.setItem(15, rewardsItem.build());

        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
