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
import java.util.UUID;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.party.Party;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class PartyCommand
implements CommandExecutor,
TabCompleter {
    private final SpazioDuelsPlugin plugin;

    public PartyCommand(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Comando solo para jugadores.");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            this.sendHelp(player);
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equalsIgnoreCase("create")) {
            if (this.plugin.getPartyManager().hasParty(player)) {
                player.sendMessage(TextUtil.colorize("&cYa perteneces a una party."));
                return true;
            }
            this.plugin.getPartyManager().createParty(player);
            player.sendMessage(TextUtil.colorize("&aParty creada exitosamente. Invita miembros con &e/party invite <jugador>"));
            return true;
        }
        if (sub.equalsIgnoreCase("invite")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /party invite <jugador>"));
                return true;
            }
            Party party = this.plugin.getPartyManager().getParty(player);
            if (party == null) {
                party = this.plugin.getPartyManager().createParty(player);
            }
            if (!party.isLeader(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cSolo el l\u00edder de la party puede invitar jugadores."));
                return true;
            }
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target == null) {
                player.sendMessage(TextUtil.colorize("&cJugador no encontrado."));
                return true;
            }
            party.invite(target);
            player.sendMessage(TextUtil.colorize("&aInvitaci\u00f3n enviada a &e" + target.getName()));
            target.sendMessage(TextUtil.colorize("&e" + player.getName() + " &7te ha invitado a su party. Usa &a/party accept " + player.getName() + " &7para unirte."));
            return true;
        }
        if (sub.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                player.sendMessage(TextUtil.colorize("&cUso: /party accept <lider>"));
                return true;
            }
            Player leader = Bukkit.getPlayer((String)args[1]);
            if (leader == null) {
                player.sendMessage(TextUtil.colorize("&cL\u00edder no encontrado."));
                return true;
            }
            Party party = this.plugin.getPartyManager().getParty(leader);
            if (party == null || !party.hasInvite(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cNo tienes invitaciones pendientes de esta party."));
                return true;
            }
            this.plugin.getPartyManager().joinParty(player, party);
            party.broadcast("&a" + player.getName() + " se ha unido a la party.");
            return true;
        }
        if (sub.equalsIgnoreCase("leave")) {
            Party party = this.plugin.getPartyManager().getParty(player);
            if (party == null) {
                player.sendMessage(TextUtil.colorize("&cNo est\u00e1s en ninguna party."));
                return true;
            }
            this.plugin.getPartyManager().leaveParty(player);
            player.sendMessage(TextUtil.colorize("&aHas salido de la party."));
            return true;
        }
        if (sub.equalsIgnoreCase("disband")) {
            Party party = this.plugin.getPartyManager().getParty(player);
            if (party == null || !party.isLeader(player.getUniqueId())) {
                player.sendMessage(TextUtil.colorize("&cSolo el l\u00edder de la party puede disolverla."));
                return true;
            }
            this.plugin.getPartyManager().disbandParty(party);
            return true;
        }
        if (sub.equalsIgnoreCase("info")) {
            Party party = this.plugin.getPartyManager().getParty(player);
            if (party == null) {
                player.sendMessage(TextUtil.colorize("&cNo est\u00e1s en ninguna party."));
                return true;
            }
            player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            player.sendMessage(TextUtil.colorize("&6&lInformaci\u00f3n de Party"));
            player.sendMessage(TextUtil.colorize("&7L\u00edder: &a" + Bukkit.getOfflinePlayer((UUID)party.getLeader()).getName()));
            player.sendMessage(TextUtil.colorize("&7Miembros (&e" + party.getSize() + "&7): &f" + party.getFormattedMembers()));
            player.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
            return true;
        }
        this.sendHelp(player);
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

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "leave", "disband", "info");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept"))) {
            ArrayList<String> players = new ArrayList<String>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                players.add(p.getName());
            }
            return players;
        }
        return new ArrayList<String>();
    }
}

