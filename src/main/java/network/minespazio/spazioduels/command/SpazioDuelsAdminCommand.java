package network.minespazio.spazioduels.command;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.gui.ArenaAdminGUI;
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

public class SpazioDuelsAdminCommand implements CommandExecutor, TabCompleter {

    private final SpazioDuelsPlugin plugin;

    public SpazioDuelsAdminCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("spazioduels.admin")) {
            sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para administrar SpazioDuels."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("setup")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Comandos de setup requieren ser ejecutados por un jugador.");
                return true;
            }

            if (args.length < 2) {
                new ArenaAdminGUI(plugin).open(player);
                return true;
            }

            String action = args[1].toLowerCase();

            if (action.equalsIgnoreCase("create")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup create <nombreArena>"));
                    return true;
                }
                String name = args[2];
                plugin.getArenaManager().createArena(name);
                player.sendMessage(TextUtil.colorize("&aArena &b" + name + " &acreada exitosamente."));
                return true;

            } else if (action.equalsIgnoreCase("delete")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup delete <nombreArena>"));
                    return true;
                }
                String name = args[2];
                if (plugin.getArenaManager().deleteArena(name)) {
                    player.sendMessage(TextUtil.colorize("&aArena &b" + name + " &aeliminada."));
                } else {
                    player.sendMessage(TextUtil.colorize("&cArena no encontrada: " + name));
                }
                return true;

            } else if (action.equalsIgnoreCase("setspawn1")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspawn1 <nombreArena>"));
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) arena = plugin.getArenaManager().createArena(args[2]);
                arena.setSpawn1(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn 1 configurado para la arena &b" + arena.getName() + "&a."));
                return true;

            } else if (action.equalsIgnoreCase("setspawn2")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspawn2 <nombreArena>"));
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) arena = plugin.getArenaManager().createArena(args[2]);
                arena.setSpawn2(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn 2 configurado para la arena &b" + arena.getName() + "&a."));
                return true;

            } else if (action.equalsIgnoreCase("setspectator")) {
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd setup setspectator <nombreArena>"));
                    return true;
                }
                Arena arena = plugin.getArenaManager().getArena(args[2]);
                if (arena == null) arena = plugin.getArenaManager().createArena(args[2]);
                arena.setSpectatorSpawn(player.getLocation());
                plugin.getArenaManager().saveArenas();
                player.sendMessage(TextUtil.colorize("&aSpawn espectador configurado para la arena &b" + arena.getName() + "&a."));
                return true;

            } else if (action.equalsIgnoreCase("setlobby")) {
                plugin.getArenaManager().setGlobalLobbySpawn(player.getLocation());
                player.sendMessage(TextUtil.colorize("&aLobby global de duelos configurado exitosamente."));
                return true;
            }

        } else if (sub.equalsIgnoreCase("kit")) {
            if (args.length < 2) {
                sender.sendMessage(TextUtil.colorize("&cUso: /sd kit <create|delete|list|togglebuild> <nombre>"));
                return true;
            }

            String action = args[1].toLowerCase();

            if (action.equalsIgnoreCase("create")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Requiere ser jugador.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(TextUtil.colorize("&cUso: /sd kit create <nombreKit>"));
                    return true;
                }
                String name = args[2];
                plugin.getKitManager().createKitFromPlayer(name, player);
                player.sendMessage(TextUtil.colorize("&aKit &b" + name + " &acreado desde tu inventario actual."));
                return true;

            } else if (action.equalsIgnoreCase("delete")) {
                if (args.length < 3) {
                    sender.sendMessage(TextUtil.colorize("&cUso: /sd kit delete <nombreKit>"));
                    return true;
                }
                String name = args[2];
                if (plugin.getKitManager().deleteKit(name)) {
                    sender.sendMessage(TextUtil.colorize("&aKit &b" + name + " &aeliminado."));
                } else {
                    sender.sendMessage(TextUtil.colorize("&cKit no encontrado: " + name));
                }
                return true;

            } else if (action.equalsIgnoreCase("togglebuild")) {
                if (args.length < 3) {
                    sender.sendMessage(TextUtil.colorize("&cUso: /sd kit togglebuild <nombreKit>"));
                    return true;
                }
                Kit kit = plugin.getKitManager().getKit(args[2]);
                if (kit != null) {
                    kit.setAllowBuilding(!kit.isAllowBuilding());
                    plugin.getKitManager().saveKits();
                    sender.sendMessage(TextUtil.colorize("&aConstrucción para el kit &b" + kit.getName() + "&a: &e" + (kit.isAllowBuilding() ? "Permitida" : "Desactivada")));
                } else {
                    sender.sendMessage(TextUtil.colorize("&cKit no encontrado."));
                }
                return true;

            } else if (action.equalsIgnoreCase("list")) {
                sender.sendMessage(TextUtil.colorize("&eKits cargados:"));
                for (Kit k : plugin.getKitManager().getKits()) {
                    sender.sendMessage(TextUtil.colorize("&7- &b" + k.getName() + " &7(Construcción: " + (k.isAllowBuilding() ? "&aSí" : "&cNo") + "&7)"));
                }
                return true;
            }

        } else if (sub.equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getKitManager().loadKits();
            plugin.getArenaManager().loadArenas();
            sender.sendMessage(TextUtil.colorize("&aSpazioDuels reloaded exitosamente."));
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        sender.sendMessage(TextUtil.colorize("&6&lSpazioDuels Admin Commands"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspawn1 <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspawn2 <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setspectator <arena>"));
        sender.sendMessage(TextUtil.colorize("&7/sd setup setlobby"));
        sender.sendMessage(TextUtil.colorize("&7/sd kit create <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/sd kit togglebuild <nombre>"));
        sender.sendMessage(TextUtil.colorize("&7/sd reload"));
        sender.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setup", "kit", "reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setup")) {
            return Arrays.asList("create", "delete", "setspawn1", "setspawn2", "setspectator", "setlobby");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kit")) {
            return Arrays.asList("create", "delete", "togglebuild", "list");
        }
        return new ArrayList<>();
    }
}
