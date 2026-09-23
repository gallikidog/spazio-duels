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
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.gui.AdminKitsGUI;
import network.minespazio.spazioduels.gui.ArenaAdminGUI;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SpazioDuelsAdminCommand
implements CommandExecutor,
TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public SpazioDuelsAdminCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spazioduels.admin")) {
            sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para administrar SpazioDuels."));
            return true;
        }
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equalsIgnoreCase("adminkits")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Requiere ser un jugador para abrir la GUI de gesti\u00f3n de kits.");
                return true;
            }
            Player player = (Player)sender;
            new AdminKitsGUI(this.plugin).open(player);
            return true;
        }
        if (sub.equalsIgnoreCase("setup")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Comandos de setup requieren ser ejecutados por un jugador.");
                return true;
            }
            Player player = (Player)sender;
            if (args.length < 2) {
                new ArenaAdminGUI(this.plugin).open(player);
                return true;
            }
            String action = args[1].toLowerCase();
            if (action.equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup create <nombreArena>"));
                    return true;
                }
                String name = args[2];
                this.plugin.getArenaManager().createArena(name);
                player.sendMessage(TextUtil.colorize("&aArena &b" + name + " &acreada exitosamente."));
                return true;
            }
            if (action.equalsIgnoreCase("delete")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup delete <nombreArena>"));
                    return true;
                }
                String name = args[2];
                if (this.plugin.getArenaManager().deleteArena(name)) {
                    player.sendMessage(TextUtil.colorize("&aArena &b" + name + " &aeliminada."));
                } else {
                    player.sendMessage(TextUtil.colorize("&cArena no encontrada: " + name));
                }
                return true;
            }
            if (action.equalsIgnoreCase("setspawn1")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspawn1 <nombreArena>"));
                    return true;
                }
                Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    arena = this.plugin.getArenaManager().createArena(args[2]);
                }
                arena.setSpawn1(player.getLocation());
                this.plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn 1 configurado para la arena &b" + arena.getName() + "&a."));
                return true;
            }
            if (action.equalsIgnoreCase("setspawn2")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspawn2 <nombreArena>"));
                    return true;
                }
                Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    arena = this.plugin.getArenaManager().createArena(args[2]);
                }
                arena.setSpawn2(player.getLocation());
                this.plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn 2 configurado para la arena &b" + arena.getName() + "&a."));
                return true;
            }
            if (action.equalsIgnoreCase("setspectator")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspectator <nombreArena>"));
                    return true;
                }
                Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                if (arena == null) {
                    arena = this.plugin.getArenaManager().createArena(args[2]);
                }
                arena.setSpectatorSpawn(player.getLocation());
                this.plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn espectador configurado para la arena &b" + arena.getName() + "&a."));
                return true;
            }
            if (action.equalsIgnoreCase("setlobby")) {
                this.plugin.getArenaManager().setGlobalLobbySpawn(player.getLocation());
                player.sendMessage(TextUtil.colorize("&aLobby global de duelos configurado exitosamente."));
                return true;
            }
        } else if (sub.equalsIgnoreCase("kit")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /sd kit <create|delete|list|togglebuild|toggleduel> <nombre>"));
                return true;
            }
            String action = args[1].toLowerCase();
            if (action.equalsIgnoreCase("create")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Requiere ser jugador.");
                    return true;
                }
                Player player = (Player)sender;
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd kit create <nombreKit>"));
                    return true;
                }
                String name = args[2];
                this.plugin.getKitManager().createKitFromPlayer(name, player);
                player.sendMessage(TextUtil.colorize("&aKit &b" + name + " &acreado desde tu inventario actual."));
                return true;
            }
            if (action.equalsIgnoreCase("delete")) {
                if (args.length < 3) {
                    sender.sendMessage(TextUtil.colorize("&cUso: /sd kit delete <nombreKit>"));
                    return true;
                }
                String name = args[2];
                if (this.plugin.getKitManager().deleteKit(name)) {
                    sender.sendMessage(TextUtil.colorize("&aKit &b" + name + " &aeliminado."));
                } else {
                    sender.sendMessage(TextUtil.colorize("&cKit no encontrado: " + name));
                }
                return true;
            }
            if (action.equalsIgnoreCase("togglebuild")) {
                if (args.length < 3) {
                    sender.sendMessage(TextUtil.colorize("&cUso: /sd kit togglebuild <nombreKit>"));
                    return true;
                }
                Kit kit = this.plugin.getKitManager().getKit(args[2]);
                if (kit != null) {
                    kit.setAllowBuilding(!kit.isAllowBuilding());
                    this.plugin.getKitManager().saveKits();
                    sender.sendMessage(TextUtil.colorize("&aConstrucci\u00f3n para el kit &b" + kit.getName() + "&a: &e" + (kit.isAllowBuilding() ? "Permitida" : "Desactivada")));
                } else {
                    sender.sendMessage(TextUtil.colorize("&cKit no encontrado."));
                }
                return true;
            }
            if (action.equalsIgnoreCase("toggleduel")) {
                if (args.length < 3) {
                    sender.sendMessage(TextUtil.colorize("&cUso: /sd kit toggleduel <nombreKit>"));
                    return true;
                }
                boolean newState = this.plugin.getKitManager().toggleKitEnabledForDuels(args[2]);
                sender.sendMessage(TextUtil.colorize("&aEl kit &b" + args[2] + " &aahora est\u00e1: " + (newState ? "&aHABILITADO" : "&cDESHABILITADO") + " &apara duelos."));
                return true;
            }
            if (action.equalsIgnoreCase("list")) {
                sender.sendMessage(TextUtil.colorize("&eKits cargados:"));
                for (Kit k : this.plugin.getKitManager().getKits()) {
                    String status = k.isEnabledForDuels() ? "&a[Habilitado]" : "&c[Deshabilitado]";
                    String origin = k.isFromPlayerKits() ? "&b(PlayerKits2)" : "&e(Nativo)";
                    sender.sendMessage(TextUtil.colorize("&7- &b" + k.getName() + " " + status + " " + origin));
                }
                return true;
            }
        } else if (sub.equalsIgnoreCase("reload")) {
            this.plugin.reloadDuelConfiguration();
            sender.sendMessage(TextUtil.colorize("&aSpazioDuels reloaded exitosamente."));
            return true;
        }
        this.sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        sender.sendMessage(TextUtil.colorize("&6&lSpazioDuels Admin Commands"));
        sender.sendMessage(TextUtil.colorize("&7/sd adminkits &f(Panel GUI de activaci\u00f3n de kits)"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspawn1 <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspawn2 <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspectator <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setlobby"));
        sender.sendMessage(TextUtil.colorize("&7/sd kit create <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/sd kit toggleduel <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/sd reload"));
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("adminkits", "setup", "kit", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setup")) {
            return Arrays.asList("create", "delete", "setspawn1", "setspawn2", "setspectator", "setlobby");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) {
            return Arrays.asList("create", "delete", "togglebuild", "toggleduel", "list");
        }
        return new ArrayList<String>();
    }
}

