package network.minespazio.spazioduels.duel;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.gui.KitSelectorGUI;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.party.Party;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, DuelMatch> activeMatchesByPlayer = new HashMap<>();
    private final Map<UUID, DuelRequest> pendingRequestsByTarget = new HashMap<>();
    private final Set<DuelMatch> activeMatches = new HashSet<>();

    public DuelManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void initiateDuelRequest(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage(TextUtil.colorize("&cNo puedes enviarte un duelo a ti mismo."));
            return;
        }

        if (isInDuel(sender)) {
            sender.sendMessage(TextUtil.colorize("&cYa estás en un duelo."));
            return;
        }

        if (isInDuel(target)) {
            sender.sendMessage(TextUtil.colorize("&cEl jugador " + target.getName() + " ya está en un duelo."));
            return;
        }

        // Open Kit Selector GUI for sender to choose kit
        KitSelectorGUI gui = new KitSelectorGUI(plugin, sender, target);
        gui.open();
    }

    public void sendDuelRequestWithKit(Player sender, Player target, Kit kit, DuelMode mode) {
        List<Player> team1 = new ArrayList<>();
        List<Player> team2 = new ArrayList<>();

        Party senderParty = plugin.getPartyManager().getParty(sender);
        Party targetParty = plugin.getPartyManager().getParty(target);

        if (senderParty != null) {
            team1.addAll(senderParty.getOnlinePlayers());
        } else {
            team1.add(sender);
        }

        if (targetParty != null) {
            team2.addAll(targetParty.getOnlinePlayers());
        } else {
            team2.add(target);
        }

        DuelRequest request = new DuelRequest(sender, target, team1, team2, kit, mode);
        pendingRequestsByTarget.put(target.getUniqueId(), request);

        sender.sendMessage(TextUtil.colorize("&aHas enviado una solicitud de duelo a &e" + target.getName() + " &acon el kit &b" + kit.getName() + "&a."));

        // Send clickable accept/deny message to target
        target.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        target.sendMessage(TextUtil.colorize("&6&lSOLICITUD DE DUELO RECIBIDA"));
        target.sendMessage(TextUtil.colorize("&fEl jugador &b" + sender.getName() + " &fte ha desafiado a un duelo."));
        target.sendMessage(TextUtil.colorize("&7Kit: &b" + kit.getName() + " &7| Modo: &b" + mode.getDisplayName()));
        target.sendMessage(TextUtil.toComponent("&a&l[ACEPTAR DUELO]").clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/duel accept " + sender.getName())));
        target.sendMessage(TextUtil.toComponent("&c&l[RECHAZAR DUELO]").clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/duel deny " + sender.getName())));
        target.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    public void acceptDuelRequest(Player target, Player sender) {
        DuelRequest request = pendingRequestsByTarget.get(target.getUniqueId());
        if (request == null || request.isExpired() || !request.getSender().equals(sender.getUniqueId())) {
            target.sendMessage(TextUtil.colorize("&cNo tienes ninguna solicitud de duelo pendiente de " + sender.getName() + "."));
            return;
        }

        pendingRequestsByTarget.remove(target.getUniqueId());

        // Find available arena
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            target.sendMessage(TextUtil.colorize("&cNo hay arenas de duelos disponibles en este momento. Inténtalo más tarde."));
            sender.sendMessage(TextUtil.colorize("&cNo hay arenas de duelos disponibles en este momento. Inténtalo más tarde."));
            return;
        }

        DuelTeam dt1 = new DuelTeam("Equipo 1", request.getTeam1());
        DuelTeam dt2 = new DuelTeam("Equipo 2", request.getTeam2());

        HandicapRule handicapRule = request.getMode().isHandicap() ? new HandicapRule(true, 1.0) : null;

        DuelMatch match = new DuelMatch(plugin, arena, request.getKit(), request.getMode(), dt1, dt2, handicapRule);
        registerMatch(match);
        match.startMatchSequence();
    }

    public void denyDuelRequest(Player target, Player sender) {
        DuelRequest request = pendingRequestsByTarget.get(target.getUniqueId());
        if (request != null && request.getSender().equals(sender.getUniqueId())) {
            pendingRequestsByTarget.remove(target.getUniqueId());
            target.sendMessage(TextUtil.colorize("&cHas rechazado el duelo de " + sender.getName() + "."));
            sender.sendMessage(TextUtil.colorize("&c" + target.getName() + " ha rechazado tu solicitud de duelo."));
        }
    }

    public void registerMatch(DuelMatch match) {
        activeMatches.add(match);
        for (Player p : match.getAllPlayers()) {
            activeMatchesByPlayer.put(p.getUniqueId(), match);
        }
    }

    public void unregisterMatch(DuelMatch match) {
        activeMatches.remove(match);
        for (UUID uuid : match.getTeam1().getMembers()) {
            activeMatchesByPlayer.remove(uuid);
        }
        for (UUID uuid : match.getTeam2().getMembers()) {
            activeMatchesByPlayer.remove(uuid);
        }
    }

    public DuelMatch getMatch(Player player) {
        if (player == null) return null;
        return activeMatchesByPlayer.get(player.getUniqueId());
    }

    public boolean isInDuel(Player player) {
        if (player == null) return false;
        return activeMatchesByPlayer.containsKey(player.getUniqueId());
    }

    public Set<DuelMatch> getActiveMatches() {
        return activeMatches;
    }
}
