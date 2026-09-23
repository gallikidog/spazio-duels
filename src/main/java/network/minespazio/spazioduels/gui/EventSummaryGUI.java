/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 */
package network.minespazio.spazioduels.gui;

import java.util.ArrayList;
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

public class EventSummaryGUI
implements InventoryHolder {
    public static final String TITLE = TextUtil.colorize("&8Resumen del Evento de Duelos");
    private final SpazioDuelsPlugin plugin;
    private final EventSummary summary;
    private Inventory inventory;

    public EventSummaryGUI(SpazioDuelsPlugin plugin, EventSummary summary) {
        this.plugin = plugin;
        this.summary = summary;
    }

    public void open(Player player) {
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)27, (String)TITLE);
        ItemBuilder glass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ");
        for (int i = 0; i < 27; ++i) {
            this.inventory.setItem(i, glass.build());
        }
        ItemBuilder trophy = new ItemBuilder(Material.NETHER_STAR).name("&6&l\u00a1GANADORES DEL EVENTO!").lore("&7Jugador/Equipo Ganador:", "&a" + this.summary.getWinnerName() + " &7(" + this.summary.getWinnerTeamName() + ")", "", "&7Modo jugado: &b" + this.summary.getModeName(), "&7Kit utilizado: &b" + this.summary.getKitName());
        this.inventory.setItem(11, trophy.build());
        ItemBuilder stats = new ItemBuilder(Material.CLOCK).name("&e&lESTAD\u00cdSTICAS DEL EVENTO").lore("&7Nombre del Evento: &f" + this.summary.getEventName(), "&7Duraci\u00f3n total: &b" + this.summary.getDurationSeconds() + " segundos", "&7Encuentros disputados: &b" + this.summary.getTotalMatches(), "&7Eliminaciones totales: &c" + this.summary.getTotalKills());
        this.inventory.setItem(13, stats.build());
        ArrayList<String> rewardLore = new ArrayList<String>();
        rewardLore.add("&7Recompensas otorgadas:");
        for (RewardUtil.RewardItem r : this.summary.getRewards()) {
            rewardLore.add("&f- " + r.getName());
        }
        ItemBuilder rewardsItem = new ItemBuilder(Material.CHEST).name("&a&lRECOMPENSAS ENTREGADAS").lore(rewardLore);
        this.inventory.setItem(15, rewardsItem.build());
        player.openInventory(this.inventory);
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

