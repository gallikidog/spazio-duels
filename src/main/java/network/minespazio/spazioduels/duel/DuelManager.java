/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.event.ClickEvent
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.event.ClickEvent;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.duel.DuelRequest;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.duel.HandicapRule;
import network.minespazio.spazioduels.gui.KitSelectorGUI;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.party.Party;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DuelManager {
    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, DuelMatch> activeMatchesByPlayer = new HashMap<UUID, DuelMatch>();
    private final Map<UUID, DuelRequest> pendingRequestsByTarget = new HashMap<UUID, DuelRequest>();
    private final Set<DuelMatch> activeMatches = new HashSet<DuelMatch>();

    public DuelManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void initiateDuelRequest(Player sender, Player target) {
        if (sender.equals((Object)target)) {
            sender.sendMessage(TextUtil.colorize("&cNo puedes enviarte un duelo a ti mismo."));
            return;
        }
        if (this.isInDuel(sender)) {
            sender.sendMessage(TextUtil.colorize("&cYa est\u00e1s en un duelo."));
            return;
        }
        if (this.isInDuel(target)) {
            sender.sendMessage(TextUtil.colorize("&cEl jugador " + target.getName() + " ya est\u00e1 en un duelo."));
            return;
        }
        if (this.plugin.getKitManager().getDirectDuelKits().isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&cNo se encontraron los kits de practica para los duelos."));
            return;
        }
        KitSelectorGUI gui = new KitSelectorGUI(this.plugin, sender, target);
        gui.open();
    }

    private Kit defaultFallbackKit() {
        ItemStack[] contents = new ItemStack[36];
        contents[0] = new ItemStack(Material.IRON_SWORD);
        contents[1] = new ItemStack(Material.BOW);
        contents[2] = new ItemStack(Material.ARROW, 16);
        contents[3] = new ItemStack(Material.COOKED_BEEF, 16);
        contents[4] = new ItemStack(Material.GOLDEN_APPLE, 2);
        ItemStack[] armor = new ItemStack[]{new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)};
        return new Kit("Default", new ItemStack(Material.IRON_SWORD), contents, armor, null, List.of(), false, true, false);
    }

    public void sendDuelRequestWithKit(Player sender, Player target, Kit kit, DuelMode mode) {
        ArrayList<Player> team1 = new ArrayList<Player>();
        ArrayList<Player> team2 = new ArrayList<Player>();
        Party senderParty = this.plugin.getPartyManager().getParty(sender);
        Party targetParty = this.plugin.getPartyManager().getParty(target);
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
        this.pendingRequestsByTarget.put(target.getUniqueId(), request);
        sender.sendMessage(TextUtil.colorize("&aHas enviado una solicitud de duelo a &e" + target.getName() + " &acon el kit &b" + kit.getName() + "&a."));
        target.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
        target.sendMessage(TextUtil.colorize("&6&lSOLICITUD DE DUELO RECIBIDA"));
        target.sendMessage(TextUtil.colorize("&fEl jugador &b" + sender.getName() + " &fte ha desafiado a un duelo."));
        target.sendMessage(TextUtil.colorize("&7Kit: &b" + kit.getName() + " &7| Modo: &b" + mode.getDisplayName()));
        target.sendMessage(TextUtil.toComponent("&a&l[ACEPTAR DUELO]").clickEvent(ClickEvent.runCommand((String)("/duel accept " + sender.getName()))));
        target.sendMessage(TextUtil.toComponent("&c&l[RECHAZAR DUELO]").clickEvent(ClickEvent.runCommand((String)("/duel deny " + sender.getName()))));
        target.sendMessage(TextUtil.colorize("&e-------------------------------------------"));
    }

    public void acceptDuelRequest(Player target, Player sender) {
        DuelRequest request = this.pendingRequestsByTarget.get(target.getUniqueId());
        if (request == null || request.isExpired() || !request.getSender().equals(sender.getUniqueId())) {
            target.sendMessage(TextUtil.colorize("&cNo tienes ninguna solicitud de duelo pendiente de " + sender.getName() + "."));
            return;
        }
        this.pendingRequestsByTarget.remove(target.getUniqueId());
        Arena arena = this.plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            target.sendMessage(TextUtil.colorize("&cNo hay arenas de duelos disponibles en este momento. Int\u00e9ntalo m\u00e1s tarde."));
            sender.sendMessage(TextUtil.colorize("&cNo hay arenas de duelos disponibles en este momento. Int\u00e9ntalo m\u00e1s tarde."));
            return;
        }
        DuelTeam dt1 = new DuelTeam("Equipo 1", request.getTeam1());
        DuelTeam dt2 = new DuelTeam("Equipo 2", request.getTeam2());
        HandicapRule handicapRule = request.getMode().isHandicap() ? new HandicapRule(true, 1.0) : null;
        DuelMatch match = new DuelMatch(this.plugin, arena, request.getKit(), request.getMode(), dt1, dt2, handicapRule);
        this.registerMatch(match);
        match.startMatchSequence();
    }

    public void denyDuelRequest(Player target, Player sender) {
        DuelRequest request = this.pendingRequestsByTarget.get(target.getUniqueId());
        if (request != null && request.getSender().equals(sender.getUniqueId())) {
            this.pendingRequestsByTarget.remove(target.getUniqueId());
            target.sendMessage(TextUtil.colorize("&cHas rechazado el duelo de " + sender.getName() + "."));
            sender.sendMessage(TextUtil.colorize("&c" + target.getName() + " ha rechazado tu solicitud de duelo."));
        }
    }

    public void registerMatch(DuelMatch match) {
        this.activeMatches.add(match);
        for (Player p : match.getAllPlayers()) {
            this.activeMatchesByPlayer.put(p.getUniqueId(), match);
        }
    }

    public void unregisterMatch(DuelMatch match) {
        this.activeMatches.remove(match);
        for (UUID uuid : match.getTeam1().getMembers()) {
            this.activeMatchesByPlayer.remove(uuid);
        }
        for (UUID uuid : match.getTeam2().getMembers()) {
            this.activeMatchesByPlayer.remove(uuid);
        }
    }

    public DuelMatch getMatch(Player player) {
        if (player == null) {
            return null;
        }
        return this.activeMatchesByPlayer.get(player.getUniqueId());
    }

    public boolean isInDuel(Player player) {
        if (player == null) {
            return false;
        }
        return this.activeMatchesByPlayer.containsKey(player.getUniqueId());
    }

    public Set<DuelMatch> getActiveMatches() {
        return this.activeMatches;
    }
}

