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
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.gui.KothLootGUI;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothCommandReward;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class KothCommand
implements CommandExecutor,
TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public KothCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spazioduels.admin.koth")) {
            sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para administrar KOTHs."));
            return true;
        }
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth create <nombre>"));
                return true;
            }
            String name = args[1];
            if (this.plugin.getKothManager().getKoth(name) != null) {
                sender.sendMessage(TextUtil.colorize("&cYa existe un KOTH con ese nombre."));
                return true;
            }
            this.plugin.getKothManager().createKoth(name);
            sender.sendMessage(TextUtil.colorize("&aKOTH &b" + name + " &acreado exitosamente."));
            return true;
        }
        if (sub.equalsIgnoreCase("delete") || sub.equalsIgnoreCase("remove")) {
            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth delete <koth> confirm"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            if (this.rejectActiveChange(sender, koth)) {
                return true;
            }
            this.plugin.getKothManager().deleteKoth(koth.getName());
            sender.sendMessage(TextUtil.colorize("&aKOTH &b" + koth.getName() + " &aeliminado exitosamente."));
            return true;
        }
        if (sub.equalsIgnoreCase("setcapdelay")) {
            if (args.length < 3) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth setcapdelay <koth> <segundos>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            if (this.rejectActiveChange(sender, koth)) {
                return true;
            }
            try {
                int seconds = Integer.parseInt(args[2]);
                if (seconds <= 0) {
                    sender.sendMessage(TextUtil.colorize("&cEl tiempo debe ser un n\u00famero entero mayor a 0."));
                    return true;
                }
                koth.setCaptureDelaySeconds(seconds);
                this.plugin.getKothManager().saveKoths();
                sender.sendMessage(TextUtil.colorize("&aTiempo de captura para &b" + koth.getName() + " &aconfigurado en &e" + seconds + " &asegundos."));
            }
            catch (NumberFormatException e) {
                sender.sendMessage(TextUtil.colorize("&cTiempo inv\u00e1lido: debe ser un n\u00famero en segundos."));
            }
            return true;
        }
        if (sub.equalsIgnoreCase("setloot")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setloot <koth>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                player.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            if (this.rejectActiveChange((CommandSender)player, koth)) {
                return true;
            }
            new KothLootGUI(this.plugin, koth).open(player);
            return true;
        }
        if (sub.equalsIgnoreCase("addcommandreward")) {
            boolean added;
            if (args.length < 3) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth addcommandreward <koth> <probabilidad> <comando>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            if (this.rejectActiveChange(sender, koth)) {
                return true;
            }
            boolean guaranteed = true;
            double chance = 100.0;
            int commandStart = 2;
            if (args.length >= 4) {
                try {
                    chance = Double.parseDouble(args[2]);
                    guaranteed = false;
                    commandStart = 3;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            String rewardCommand = String.join((CharSequence)" ", Arrays.copyOfRange(args, commandStart, args.length));
            boolean bl = added = guaranteed ? koth.addGuaranteedCommandReward(rewardCommand) : koth.addCommandReward(rewardCommand, chance);
            if (!added) {
                sender.sendMessage(TextUtil.colorize("&cComando o probabilidad invalida. Maximo 3 recompensas y 100% total."));
                return true;
            }
            this.plugin.getKothManager().saveKoths();
            String chanceLabel = guaranteed ? "garantizada" : String.format("%.2f%%", chance);
            sender.sendMessage(TextUtil.colorize("&aRecompensa por consola " + chanceLabel + " agregada al KOTH &b" + koth.getName() + "&a."));
            return true;
        }
        if (sub.equalsIgnoreCase("removecommandreward")) {
            if (args.length < 3) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth removecommandreward <koth> <indice>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            if (this.rejectActiveChange(sender, koth)) {
                return true;
            }
            try {
                int index = Integer.parseInt(args[2]) - 1;
                if (!koth.removeCommandReward(index)) {
                    sender.sendMessage(TextUtil.colorize("&cIndice invalido. Usa /koth listcommandrewards " + koth.getName()));
                    return true;
                }
                this.plugin.getKothManager().saveKoths();
                sender.sendMessage(TextUtil.colorize("&aRecompensa por consola eliminada."));
            }
            catch (NumberFormatException exception) {
                sender.sendMessage(TextUtil.colorize("&cEl indice debe ser un numero entero."));
            }
            return true;
        }
        if (sub.equalsIgnoreCase("listcommandrewards")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth listcommandrewards <koth>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            sender.sendMessage(TextUtil.colorize("&eRecompensas por consola de &b" + koth.getName() + "&e:"));
            if (koth.getCommandRewards().isEmpty()) {
                sender.sendMessage(TextUtil.colorize("&7No hay recompensas por consola configuradas."));
            } else {
                for (int index = 0; index < koth.getCommandRewards().size(); ++index) {
                    KothCommandReward reward = koth.getCommandRewards().get(index);
                    String chance = reward.guaranteed() ? "&aGarantizada" : "&e" + String.format("%.2f%%", reward.chance());
                    sender.sendMessage(TextUtil.colorize("&7" + (index + 1) + ". " + chance + " &f/" + reward.command()));
                }
            }
            return true;
        }
        if (sub.equalsIgnoreCase("move") || sub.equalsIgnoreCase("relocate")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth move <koth> <zona|captura>"));
                return true;
            }
            if (args[2].equalsIgnoreCase("zona") || args[2].equalsIgnoreCase("zone")) {
                return this.updateRegion((Player)sender, args[1], false);
            }
            if (args[2].equalsIgnoreCase("captura") || args[2].equalsIgnoreCase("capzone")) {
                return this.updateRegion((Player)sender, args[1], true);
            }
            sender.sendMessage(TextUtil.colorize("&cTipo invalido. Usa &ezona &co &ecaptura&c."));
            return true;
        }
        if (sub.equalsIgnoreCase("setzone")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setzone <koth>"));
                return true;
            }
            return this.updateRegion(player, args[1], false);
        }
        if (sub.equalsIgnoreCase("setcapzone")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Comando solo para jugadores.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /koth setcapzone <koth>"));
                return true;
            }
            return this.updateRegion(player, args[1], true);
        }
        if (sub.equalsIgnoreCase("start") || sub.equalsIgnoreCase("iniciar") || sub.equalsIgnoreCase("activar")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth start <koth>"));
                return true;
            }
            String name = args[1];
            if (this.plugin.getKothManager().startKoth(name)) {
                sender.sendMessage(TextUtil.colorize("&aKOTH &b" + name + " &ainiciado exitosamente."));
            } else {
                Koth koth = this.plugin.getKothManager().getKoth(name);
                if (koth == null) {
                    sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + name + ". Usa &e/koth list&c."));
                } else if (koth.getCapZone() == null) {
                    sender.sendMessage(TextUtil.colorize("&cNo se pudo iniciar el KOTH " + name + ": falta configurar la zona de captura con &e/koth setcapzone " + name + "&c."));
                } else {
                    sender.sendMessage(TextUtil.colorize("&cNo se pudo iniciar el KOTH " + name + ". Revisa la configuracion del KOTH."));
                }
            }
            return true;
        }
        if (sub.equalsIgnoreCase("stop") || sub.equalsIgnoreCase("detener")) {
            this.plugin.getKothManager().stopActiveMatch();
            sender.sendMessage(TextUtil.colorize("&aKOTH activo detenido."));
            return true;
        }
        if (sub.equalsIgnoreCase("reload") || sub.equalsIgnoreCase("recargar")) {
            this.plugin.getKothManager().loadKoths();
            sender.sendMessage(TextUtil.colorize("&aKOTHs recargados desde koths.yml."));
            return true;
        }
        if (sub.equalsIgnoreCase("list")) {
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            sender.sendMessage(TextUtil.colorize("&6&lKOTHs Registrados"));
            for (Koth k : this.plugin.getKothManager().getKoths()) {
                String status = k.isReady() ? "&a[Listo]" : "&c[Incompleto]";
                sender.sendMessage(TextUtil.colorize("&7- &b" + k.getName() + " " + status + " &7| Cap: &e" + k.getCaptureDelaySeconds() + "s &7| Loot: &e" + k.getLootItems().size() + " \u00edtems"));
            }
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;
        }
        if (sub.equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /koth info <koth>"));
                return true;
            }
            Koth koth = this.plugin.getKothManager().getKoth(args[1]);
            if (koth == null) {
                sender.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + args[1]));
                return true;
            }
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            sender.sendMessage(TextUtil.colorize("&6&lInformaci\u00f3n de KOTH: &b" + koth.getName()));
            sender.sendMessage(TextUtil.colorize("&7Tiempo de Captura: &e" + koth.getCaptureDelaySeconds() + " segundos"));
            sender.sendMessage(TextUtil.colorize("&7Zona General: " + (koth.getZone() != null ? "&aConfigurada" : "&cNo configurada")));
            sender.sendMessage(TextUtil.colorize("&7Zona de Captura: " + (koth.getCapZone() != null ? "&aConfigurada" : "&cNo configurada")));
            sender.sendMessage(TextUtil.colorize("&7Objetos de Bot\u00edn: &e" + koth.getLootItems().size() + " \u00edtems"));
            sender.sendMessage(TextUtil.colorize("&7Comandos de Recompensa: &e" + koth.getCommandRewards().size()));
            sender.sendMessage(TextUtil.colorize("&7Probabilidad total del sorteo: &e" + String.format("%.2f%%", koth.getWeightedChanceTotal())));
            sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;
        }
        this.sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        sender.sendMessage(TextUtil.colorize("&6&lComandos de KOTH"));
        sender.sendMessage(TextUtil.colorize("&7/koth create <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/koth setcapdelay <koth> <segundos>"));
        sender.sendMessage(TextUtil.colorize("&7/koth setloot <koth> &f(Abre GUI para depositar loot)"));
        sender.sendMessage(TextUtil.colorize("&7/koth addcommandreward <koth> <probabilidad> <comando>"));
        sender.sendMessage(TextUtil.colorize("&7/koth removecommandreward <koth> <indice>"));
        sender.sendMessage(TextUtil.colorize("&7/koth listcommandrewards <koth>"));
        sender.sendMessage(TextUtil.colorize("&7/koth setzone <koth> &f(Usa selecci\u00f3n de WorldEdit)"));
        sender.sendMessage(TextUtil.colorize("&7/koth setcapzone <koth> &f(Usa selecci\u00f3n de WorldEdit)"));
        sender.sendMessage(TextUtil.colorize("&7/koth move <koth> <zona|captura> &f(Usa seleccion de WorldEdit)"));
        sender.sendMessage(TextUtil.colorize("&7/koth delete <koth> confirm"));
        sender.sendMessage(TextUtil.colorize("&7/koth start <koth>"));
        sender.sendMessage(TextUtil.colorize("&7/koth stop"));
        sender.sendMessage(TextUtil.colorize("&7/koth list"));
        sender.sendMessage(TextUtil.colorize("&7/koth info <koth>"));
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "remove", "setcapdelay", "setloot", "addcommandreward", "removecommandreward", "listcommandrewards", "setzone", "setcapzone", "move", "start", "activar", "stop", "reload", "list", "info");
        }
        if (!(args.length != 2 || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("reload"))) {
            ArrayList<String> list = new ArrayList<String>();
            for (Koth k : this.plugin.getKothManager().getKoths()) {
                list.add(k.getName());
            }
            return list;
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("move") || args[0].equalsIgnoreCase("relocate"))) {
            return Arrays.asList("zona", "captura");
        }
        return new ArrayList<String>();
    }

    private boolean updateRegion(Player player, String name, boolean captureZone) {
        Koth koth = this.plugin.getKothManager().getKoth(name);
        if (koth == null) {
            player.sendMessage(TextUtil.colorize("&cKOTH no encontrado: " + name));
            return true;
        }
        if (this.rejectActiveChange((CommandSender)player, koth)) {
            return true;
        }
        CuboidRegion selection = this.plugin.getKothManager().getPlayerWorldEditSelection(player);
        if (selection == null) {
            player.sendMessage(TextUtil.colorize("&cDebes seleccionar un area con WorldEdit/FAWE (//wand) primero."));
            return true;
        }
        if (captureZone) {
            koth.setCapZone(selection);
            player.sendMessage(TextUtil.colorize("&aZona de captura del KOTH &b" + koth.getName() + " &aconfigurada: " + this.describeRegion(selection)));
        } else {
            koth.setZone(selection);
            player.sendMessage(TextUtil.colorize("&aZona general del KOTH &b" + koth.getName() + " &aconfigurada: " + this.describeRegion(selection)));
        }
        this.plugin.getKothManager().saveKoths();
        return true;
    }

    private boolean rejectActiveChange(CommandSender sender, Koth koth) {
        if (!this.plugin.getKothManager().isActiveKoth(koth.getName())) {
            return false;
        }
        sender.sendMessage(TextUtil.colorize("&cNo puedes modificar el KOTH activo. Detenlo primero con &e/koth stop&c."));
        return true;
    }

    private String describeRegion(CuboidRegion region) {
        return "&7" + region.getWorldName() + " &f[&e" + region.getMinX() + "&f, &e" + region.getMinY() + "&f, &e" + region.getMinZ() + "&f] -> [&e" + region.getMaxX() + "&f, &e" + region.getMaxY() + "&f, &e" + region.getMaxZ() + "&f]";
    }
}

