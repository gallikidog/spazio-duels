/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.event.DuelEvent;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class DuelEventCommand
implements CommandExecutor,
TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public DuelEventCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(TextUtil.colorize("&eUso: /duelevent <start|join|summary>"));
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equalsIgnoreCase("start") || sub.equalsIgnoreCase("iniciar")) {
            if (!sender.hasPermission("spazioduels.admin.event")) {
                sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para ejecutar este comando."));
                return true;
            }
            DuelMode mode = DuelMode.SOLO_1V1;
            if (args.length >= 2) {
                mode = DuelMode.fromString(args[1]);
            }
            Kit kit = null;
            if (args.length >= 3) {
                kit = this.plugin.getKitManager().getKit(args[2]);
            }
            if (kit == null) {
                kit = this.plugin.getKitManager().getKits().stream().findFirst().orElse(null);
            }
            DuelEvent event = this.plugin.getDuelEventManager().startEvent("Evento de Duelos", mode, kit);
            sender.sendMessage(TextUtil.colorize("&a\u00a1Evento de Duelos iniciado exitosamente en modo " + mode.getDisplayName() + "!"));
            return true;
        }
        if (sub.equalsIgnoreCase("join") || sub.equalsIgnoreCase("unirse")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Solo jugadores pueden unirse al evento.");
                return true;
            }
            Player player = (Player)sender;
            DuelEvent activeEvent = this.plugin.getDuelEventManager().getActiveEvent();
            if (activeEvent == null) {
                player.sendMessage(TextUtil.colorize("&cNo hay ning\u00fan evento de duelos activo en este momento."));
                return true;
            }
            activeEvent.registerPlayer(player);
            return true;
        }
        if (sub.equalsIgnoreCase("summary") || sub.equalsIgnoreCase("resumen")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Solo jugadores pueden abrir la interfaz de resumen.");
                return true;
            }
            Player player = (Player)sender;
            UUID eventId = null;
            if (args.length >= 2) {
                try {
                    eventId = UUID.fromString(args[1]);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            this.plugin.getDuelEventManager().openSummaryGUI(player, eventId);
            return true;
        }
        sender.sendMessage(TextUtil.colorize("&eUso: /duelevent <start|join|summary>"));
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "join", "summary");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            ArrayList<String> modes = new ArrayList<String>();
            for (DuelMode m : DuelMode.values()) {
                modes.add(m.name().toLowerCase());
            }
            return modes;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            ArrayList<String> kits = new ArrayList<String>();
            for (Kit k : this.plugin.getKitManager().getKits()) {
                kits.add(k.getName());
            }
            return kits;
        }
        return new ArrayList<String>();
    }
}

