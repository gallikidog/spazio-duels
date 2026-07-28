package network.minespazio.spazioduels.command;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final SpazioDuelsPlugin plugin;

    public DuelCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

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
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cEl jugador " + args[1] + " no está en línea."));
                return true;
            }
            plugin.getDuelManager().acceptDuelRequest(player, target);
            return true;

        } else if (sub.equalsIgnoreCase("deny") || sub.equalsIgnoreCase("rechazar")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /duel deny <jugador>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cEl jugador " + args[1] + " no está en línea."));
                return true;
            }
            plugin.getDuelManager().denyDuelRequest(player, target);
            return true;

        } else if (sub.equalsIgnoreCase("forfeit") || sub.equalsIgnoreCase("rendirse")) {
            DuelMatch match = plugin.getDuelManager().getMatch(player);
            if (match == null) {
                player.sendMessage(TextUtil.colorize("&cNo estás en ningún duelo actualmente."));
                return true;
            }
            match.handleForfeit(player);
            return true;
        }

        // Direct player duel request (/duel <playerName>)
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(TextUtil.colorize("&cJugador no encontrado o desconectado: " + args[0]));
            return true;
        }

        plugin.getDuelManager().initiateDuelRequest(player, target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("accept", "deny", "forfeit"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getName().equalsIgnoreCase(sender.getName())) {
                    completions.add(p.getName());
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
