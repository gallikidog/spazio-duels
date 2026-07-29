package network.minespazio.spazioduels.command;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.gui.KothLootGUI;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KothCommand implements CommandExecutor, TabCompleter {

    private final SpazioDuelsPlugin plugin;

    public KothCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spazioduels.admin.koth")) {
            sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para administrar KOTHs."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth create <nombre>"));
                return true;
            }
            String name = args[1];
            if (plugin.getKothManager().getKoth(name) != null) {
                sender.sendMessage(TextUtil.colorize("&cYa existe un KOTH con ese nombre."));
                return true;
            }
            plugin.getKothManager().createKoth(name);
            sender.sendMessage(TextUtil.colorize("&aKOTH &b" + name + " &acreado exitosamente."));
            return true;

        } else if (sub.equalsIgnoreCase("setcapdelay")) {
            if (args.length < 3) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth setcapdelay <koth> <segundos>"));
                return true;
            }
            Koth koth = plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            try {
                int seconds = Integer.parseInt(args[2]);
                if (seconds <= 0) {
                    sender.sendMessage(TextUtil.colorize("&cEl tiempo debe ser un número entero mayor a 0."));
                    return true;
                }
                koth.setCaptureDelaySeconds(seconds);
                plugin.getKothManager().saveKoths();
                sender.sendMessage(TextUtil.colorize("&aTiempo de captura para &b" + koth.getName() + " &aconfigurado en &e" + seconds + " &asegundos."));
            } catch (NumberFormatException e) {
                sender.sendMessage(TextUtil.colorize("&cTiempo inválido: debe ser un número en segundos."));
            }
            return true;

        } else if (sub.equalsIgnoreCase("setloot")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setloot <koth>"));
                return true;
            }
            Koth koth = plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                player.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }

            // Open KOTH Loot Deposit GUI
            new KothLootGUI(plugin, koth).open(player);
            return true;

        } else if (sub.equalsIgnoreCase("setzone")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setzone <koth>"));
                return true;
            }
            Koth koth = plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                player.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }

            CuboidRegion selection = plugin.getKothManager().getPlayerWorldEditSelection(player);
            if (selection == null) {
                player.sendMessage(TextUtil.colorize("&cDebes seleccionar un área con WorldEdit/FAWE (//wand) primero."));
                return true;
            }

            koth.setZone(selection);
            plugin.getKothManager().saveKoths();
            player.sendMessage(TextUtil.colorize("&aZona general del KOTH &b" + koth.getName() + " &aconfigurada desde tu selección de WorldEdit!"));
            return true;

        } else if (sub.equalsIgnoreCase("setcapzone")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setcapzone <koth>"));
                return true;
            }
            Koth koth = plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                player.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }

            CuboidRegion selection = plugin.getKothManager().getPlayerWorldEditSelection(player);
            if (selection == null) {
                player.sendMessage(TextUtil.colorize("&cDebes seleccionar un área con WorldEdit/FAWE (//wand) primero."));
                return true;
            }

            koth.setCapZone(selection);
            plugin.getKothManager().saveKoths();
            player.sendMessage(TextUtil.colorize("&aZona de captura (CapZone) del KOTH &b" + koth.getName() + " &aconfigurada desde tu selección de WorldEdit!"));
            return true;

        } else if (sub.equalsIgnoreCase("start")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth start <koth>"));
                return true;
            }
            String name = args[1];
            if (plugin.getKothManager().startKoth(name)) {
                sender.sendMessage(TextUtil.colorize("&aKOTH &b" + name + " &ainiciado exitosamente."));
            } else {
                sender.sendMessage(TextUtil.colorize("&cNo se pudo iniciar el KOTH " + name + ". Verifica que exista y tenga una zona de captura configurada."));
            }
            return true;

        } else if (sub.equalsIgnoreCase("stop")) {
            plugin.getKothManager().stopActiveMatch();
            sender.sendMessage(TextUtil.colorize("&aKOTH activo detenido."));
            return true;

        } else if (sub.equalsIgnoreCase("list")) {
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            sender.sendMessage(TextUtil.colorize("&6&lKOTHs Registrados"));
            for (Koth k : plugin.getKothManager().getKoths()) {
                String status = k.isReady() ? "&a[Listo]" : "&c[Incompleto]";
                sender.sendMessage(TextUtil.colorize("&7- &b" + k.getName() + " " + status + " &7| Cap: &e" + k.getCaptureDelaySeconds() + "s &7| Loot: &e" + k.getLootItems().size() + " ítems"));
            }
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;

        } else if (sub.equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth info <koth>"));
                return true;
            }
            Koth koth = plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }

            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            sender.sendMessage(TextUtil.colorize("&6&lInformación de KOTH: &b" + koth.getName()));
            sender.sendMessage(TextUtil.colorize("&7Tiempo de Captura: &e" + koth.getCaptureDelaySeconds() + " segundos"));
            sender.sendMessage(TextUtil.colorize("&7Zona General: " + (koth.getZone() != null ? "&aConfigurada" : "&cNo configurada")));
            sender.sendMessage(TextUtil.colorize("&7Zona de Captura: " + (koth.getCapZone() != null ? "&aConfigurada" : "&cNo configurada")));
            sender.sendMessage(TextUtil.colorize("&7Objetos de Botín: &e" + koth.getLootItems().size() + " ítems"));
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        sender.sendMessage(TextUtil.colorize("&6&lComandos de KOTH"));
        sender.sendMessage(TextUtil.colorize("&7/koth create <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/koth setcapdelay <koth> <segundos>"));
        sender.sendMessage(TextUtil.colorize("&7/koth setloot <koth> &f(Abre GUI para depositar loot)"));
        sender.sendMessage(TextUtil.colorize("&7/koth setzone <koth> &f(Usa selección de WorldEdit)"));
        sender.sendMessage(TextUtil.colorize("&7/koth setcapzone <koth> &f(Usa selección de WorldEdit)"));
        sender.sendMessage(TextUtil.colorize("&7/koth start <koth>"));
        sender.sendMessage(TextUtil.colorize("&7/koth stop"));
        sender.sendMessage(TextUtil.colorize("&7/koth list"));
        sender.sendMessage(TextUtil.colorize("&7/koth info <koth>"));
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "setcapdelay", "setloot", "setzone", "setcapzone", "start", "stop", "list", "info");
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("stop") && !args[0].equalsIgnoreCase("list")) {
            List<String> list = new ArrayList<>();
            for (Koth k : plugin.getKothManager().getKoths()) {
                list.add(k.getName());
            }
            return list;
        }
        return new ArrayList<>();
    }
}
