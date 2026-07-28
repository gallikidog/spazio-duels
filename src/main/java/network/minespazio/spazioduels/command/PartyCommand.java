package network.minespazio.spazioduels.command;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.party.Party;
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

public class PartyCommand implements CommandExecutor, TabCompleter {

    private final SpazioDuelsPlugin plugin;

    public PartyCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Comando solo para jugadores.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("create")) {
            if (plugin.getPartyManager().hasParty(player)) {
                player.sendMessage(TextUtil.colorize("&cYa perteneces a una party."));
                return true;
            }
            plugin.getPartyManager().createParty(player);
            player.sendMessage(TextUtil.colorize("&aParty creada exitosamente. Invita miembros con &e/party invite <jugador>"));
            return true;

        } else if (sub.equalsIgnoreCase("invite")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /party invite <jugador>"));
                return true;
            }
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null) {
                party = plugin.getPartyManager().createParty(player);
            }
            if (!party.isLeader(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cSolo el líder de la party puede invitar jugadores."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cJugador no encontrado."));
                return true;
            }
            party.invite(target);
            player.sendMessage(TextUtil.colorize("&aInvitación enviada a &e" + target.getName()));
            target.sendMessage(TextUtil.colorize("&e" + player.getName() + " &7te ha invitado a su party. Usa &a/party accept " + player.getName() + " &7para unirte."));
            return true;

        } else if (sub.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /party accept <lider>"));
                return true;
            }
            Player leader = Bukkit.getPlayer(args[1]);
            if (leader == null) {
                player.sendMessage(TextUtil.colorize("&cLíder no encontrado."));
                return true;
            }
            Party party = plugin.getPartyManager().getParty(leader);
            if (party == null || !party.hasInvite(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cNo tienes invitaciones pendientes de esta party."));
                return true;
            }
            plugin.getPartyManager().joinParty(player, party);
            party.broadcast("&a" + player.getName() + " se ha unido a la party.");
            return true;

        } else if (sub.equalsIgnoreCase("leave")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null) {
                player.sendMessage(TextUtil.colorize("&cNo estás en ninguna party."));
                return true;
            }
            plugin.getPartyManager().leaveParty(player);
            player.sendMessage(TextUtil.colorize("&aHas salido de la party."));
            return true;

        } else if (sub.equalsIgnoreCase("disband")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null || !party.isLeader(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cSolo el líder de la party puede disolverla."));
                return true;
            }
            plugin.getPartyManager().disbandParty(party);
            return true;

        } else if (sub.equalsIgnoreCase("info")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null) {
                player.sendMessage(TextUtil.colorize("&cNo estás en ninguna party."));
                return true;
            }
            player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            player.sendMessage(TextUtil.colorize("&6&lInformación de Party"));
            player.sendMessage(TextUtil.colorize("&7Líder: &a" + Bukkit.getOfflinePlayer(party.getLeader()).getName()));
            player.sendMessage(TextUtil.colorize("&7Miembros (&e" + party.getSize() + "&7): &f" + party.getFormattedMembers()));
            player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        player.sendMessage(TextUtil.colorize("&6&lParty Commands"));
        player.sendMessage(TextUtil.colorize("&7/party create"));
        player.sendMessage(TextUtil.colorize("&7/party invite <jugador>"));
        player.sendMessage(TextUtil.colorize("&7/party accept <lider>"));
        player.sendMessage(TextUtil.colorize("&7/party leave"));
        player.sendMessage(TextUtil.colorize("&7/party disband"));
        player.sendMessage(TextUtil.colorize("&7/party info"));
        player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "leave", "disband", "info");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept"))) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            return players;
        }
        return new ArrayList<>();
    }
}
