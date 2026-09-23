package network.minespazio.spazioduels.command;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.event.DuelEvent;
import network.minespazio.spazioduels.event.EventState;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DuelEventCommand implements CommandExecutor, TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public DuelEventCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("start") || sub.equals("iniciar")) {
            if (!sender.hasPermission("spazioduels.admin.event") && !sender.hasPermission("spazioduels.admin")) {
                sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para iniciar un evento de duelos."));
                return true;
            }

            DuelEvent activeEvent = this.plugin.getDuelEventManager().getActiveEvent();
            if (activeEvent != null && activeEvent.getState() != EventState.FINISHED) {
                sender.sendMessage(TextUtil.colorize("&cYa hay un evento de duelos activo en este momento."));
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

            this.plugin.getDuelEventManager().startEvent("Evento de Duelos", mode, kit);
            sender.sendMessage(TextUtil.colorize("&a¡Evento de Duelos iniciado exitosamente en modo " + mode.getDisplayName() + "!"));
            return true;
        }

        if (sub.equals("cancel") || sub.equals("cancelar") || sub.equals("stop")) {
            if (!sender.hasPermission("spazioduels.admin.event") && !sender.hasPermission("spazioduels.admin")) {
                sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para cancelar el evento de duelos."));
                return true;
            }

            DuelEvent activeEvent = this.plugin.getDuelEventManager().getActiveEvent();
            if (activeEvent == null || activeEvent.getState() == EventState.FINISHED) {
                sender.sendMessage(TextUtil.colorize("&cNo hay ningún evento de duelos activo para cancelar."));
                return true;
            }

            activeEvent.cancelDueToTimeout();
            sender.sendMessage(TextUtil.colorize("&cEl evento de duelos ha sido cancelado por la administración."));
            return true;
        }

        if (sub.equals("join") || sub.equals("unirse")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.colorize("&cSolo jugadores pueden unirse al evento."));
                return true;
            }

            DuelEvent activeEvent = this.plugin.getDuelEventManager().getActiveEvent();
            if (activeEvent == null || activeEvent.getState() == EventState.FINISHED) {
                player.sendMessage(TextUtil.colorize("&cNo hay ningún evento de duelos activo en este momento."));
                return true;
            }

            activeEvent.registerPlayer(player);
            return true;
        }

        if (sub.equals("summary") || sub.equals("resumen")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(TextUtil.colorize("&cSolo jugadores pueden abrir la interfaz de resumen."));
                return true;
            }

            UUID eventId = null;
            if (args.length >= 2) {
                try {
                    eventId = UUID.fromString(args[1]);
                } catch (IllegalArgumentException ignored) {
                }
            }

            this.plugin.getDuelEventManager().openSummaryGUI(player, eventId);
            return true;
        }

        sendHelpMessage(sender);
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        if (sender.hasPermission("spazioduels.admin.event") || sender.hasPermission("spazioduels.admin")) {
            sender.sendMessage(TextUtil.colorize("&eUso Admin: &f/duelevent <start|cancel|join|summary>"));
        } else {
            sender.sendMessage(TextUtil.colorize("&eUso: &f/duelevent <join|summary>"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean isAdmin = sender.hasPermission("spazioduels.admin.event") || sender.hasPermission("spazioduels.admin");

        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("join", "summary"));
            if (isAdmin) {
                options.add("start");
                options.add("cancel");
            }
            return options;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start") && isAdmin) {
            List<String> modes = new ArrayList<>();
            for (DuelMode m : DuelMode.values()) {
                modes.add(m.name().toLowerCase(Locale.ROOT));
            }
            return modes;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start") && isAdmin) {
            List<String> kits = new ArrayList<>();
            for (Kit k : this.plugin.getKitManager().getKits()) {
                kits.add(k.getName());
            }
            return kits;
        }

        return new ArrayList<>();
    }
}
