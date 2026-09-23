/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
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
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class DuelCommand
implements CommandExecutor,
TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public DuelCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("spazioduels.admin")) {
                sender.sendMessage(TextUtil.colorize("&cNo tienes permisos para recargar SpazioDuels."));
                return true;
            }
            this.plugin.reloadDuelConfiguration();
            sender.sendMessage(TextUtil.colorize("&aConfiguracion de duelos recargada exitosamente."));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            player.sendMessage(TextUtil.colorize("&eUso: /duel <jugador|accept|deny|forfeit>"));
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equalsIgnoreCase("accept") || sub.equalsIgnoreCase("aceptar")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /duel accept <jugador>"));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cEl jugador " + args[1] + " no est\u00e1 en l\u00ednea."));
                return true;
            }
            this.plugin.getDuelManager().acceptDuelRequest(player, target);
            return true;
        }
        if (sub.equalsIgnoreCase("deny") || sub.equalsIgnoreCase("rechazar")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /duel deny <jugador>"));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cEl jugador " + args[1] + " no est\u00e1 en l\u00ednea."));
                return true;
            }
            this.plugin.getDuelManager().denyDuelRequest(player, target);
            return true;
        }
        if (sub.equalsIgnoreCase("forfeit") || sub.equalsIgnoreCase("rendirse")) {
            DuelMatch match = this.plugin.getDuelManager().getMatch(player);
            if (match == null) {
                player.sendMessage(TextUtil.colorize("&cNo est\u00e1s en ning\u00fan duelo actualmente."));
                return true;
            }
            match.handleForfeit(player);
            return true;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null) {
            player.sendMessage(TextUtil.colorize("&cJugador no encontrado o desconectado: " + args[0]));
            return true;
        }
        this.plugin.getDuelManager().initiateDuelRequest(player, target);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> completions = new ArrayList<String>(Arrays.asList("accept", "deny", "forfeit"));
            if (sender.hasPermission("spazioduels.admin")) {
                completions.add("reload");
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equalsIgnoreCase(sender.getName())) continue;
                completions.add(p.getName());
            }
            return completions;
        }
        return new ArrayList<String>();
    }
}

