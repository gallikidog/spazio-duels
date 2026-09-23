/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package network.minespazio.spazioduels.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import net.kyori.adventure.text.Component;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.event.EventState;
import network.minespazio.spazioduels.event.EventSummary;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.RewardUtil;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelEvent {
    private final UUID eventId;
    private final SpazioDuelsPlugin plugin;
    private final String eventName;
    private final DuelMode mode;
    private final Kit kit;
    private final List<RewardUtil.RewardItem> randomRewards;
    private final boolean automatic;
    private EventState state;
    private final Set<UUID> registeredPlayers = new LinkedHashSet<UUID>();
    private final List<DuelTeam> activeTeams = new ArrayList<DuelTeam>();
    private final List<DuelMatch> eventMatches = new ArrayList<DuelMatch>();
    private final long startTime;
    private long endTime;
    private String winnerName = "Ninguno";
    private String winnerTeamName = "Sin Equipo";
    private int totalMatches = 0;
    private int totalKills = 0;

    public DuelEvent(SpazioDuelsPlugin plugin, String eventName, DuelMode mode, Kit kit, List<RewardUtil.RewardItem> rewardsPool, boolean automatic) {
        this.eventId = UUID.randomUUID();
        this.plugin = plugin;
        this.eventName = eventName;
        this.mode = mode;
        this.kit = kit;
        this.automatic = automatic;
        this.state = EventState.WAITING_PLAYERS;
        this.startTime = System.currentTimeMillis();
        this.randomRewards = RewardUtil.getRandomRewards(rewardsPool, 3);
    }

    public void announceStart() {
        List<String> broadcastTemplate = this.plugin.getConfig().getStringList("event.start_broadcast");
        if (broadcastTemplate == null || broadcastTemplate.isEmpty()) {
            broadcastTemplate = Arrays.asList("-------------------------------------------", "              EVENTO DE DUELOS", "", "\t Un evento de duelos iniciara en breves.", "", "\t Modo seleccionado: %spazioduels_mode%", "\t Kit seleccionado: %spazioduels_kitselect%", "", "\t Recompensa:", "\t - %spazioduels_randomreward%", "\t - %spazioduels_randomreward%", "\t - %spazioduels_randomreward%", "", "\t - CLICK PARA INGRESAR AL EVENTO -", "-------------------------------------------");
        }
        int rewardIdx = 0;
        for (String rawLine : broadcastTemplate) {
            String line = rawLine.replace("%spazioduels_mode%", this.mode.getDisplayName()).replace("%spazioduels_kitselect%", this.kit != null ? this.kit.getName() : "Standard").replace("%spazioduels_event%", this.eventName);
            while (line.contains("%spazioduels_randomreward%")) {
                String rewardName = rewardIdx < this.randomRewards.size() ? this.randomRewards.get(rewardIdx++).getName() : "&7Recompensa sorpresa";
                line = line.replaceFirst("%spazioduels_randomreward%", Matcher.quoteReplacement(rewardName));
            }
            if (line.contains("- CLICK PARA INGRESAR AL EVENTO -")) {
                Component clickComponent = TextUtil.createClickableComponent(line, "/duelevent join", "&a\u00a1Haz clic para unirte al Evento de Duelos!");
                Bukkit.broadcast((Component)clickComponent);
                continue;
            }
            Bukkit.broadcast((Component)TextUtil.toComponent(line));
        }
    }

    public boolean registerPlayer(Player player) {
        if (this.state != EventState.WAITING_PLAYERS) {
            player.sendMessage(TextUtil.colorize("&cEl evento ya ha comenzado o finalizado."));
            return false;
        }
        if (this.plugin.getDuelManager().isInDuel(player)) {
            player.sendMessage(TextUtil.colorize("&cNo puedes unirte al evento mientras estás en un duelo."));
            return false;
        }
        if (this.registeredPlayers.contains(player.getUniqueId())) {
            player.sendMessage(TextUtil.colorize("&cYa estás registrado en este evento."));
            return false;
        }
        if (this.registeredPlayers.size() >= this.getMaxParticipants()) {
            player.sendMessage(TextUtil.colorize("&cEl evento ya tiene todos sus cupos ocupados."));
            return false;
        }
        this.registeredPlayers.add(player.getUniqueId());
        player.sendMessage(TextUtil.colorize("&a¡Te has unido exitosamente al evento de duelos!"));
        if (this.plugin.getConfig().getBoolean("event.auto_start_on_full", true) && this.registeredPlayers.size() >= this.getMaxParticipants()) {
            Bukkit.broadcast((Component)TextUtil.toComponent("&aEl Evento de Duelos inicio porque todos los cupos fueron ocupados."));
            this.startTournament();
        }
        return true;
    }

    private int getMaxParticipants() {
        int configuredLimit = this.plugin.getConfig().getInt("event.max_participants", 0);
        if (configuredLimit > 0) {
            return Math.max(2, configuredLimit);
        }
        return this.mode.getTeam1Size() + this.mode.getTeam2Size();
    }

    public synchronized void startTournament() {
        if (this.state != EventState.WAITING_PLAYERS) {
            return;
        }
        if (this.registeredPlayers.size() < 2) {
            Bukkit.broadcast((Component)TextUtil.toComponent("&cEl Evento de Duelos ha sido cancelado por falta de participantes."));
            this.state = EventState.FINISHED;
            return;
        }
        this.state = EventState.RUNNING_TOURNAMENT;
        Bukkit.broadcast((Component)TextUtil.toComponent("&a¡El Evento de Duelos ha comenzado con &e" + this.registeredPlayers.size() + " &ajugadores!"));
        ArrayList<Player> playersList = new ArrayList<Player>();
        for (UUID uuid : this.registeredPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            playersList.add(p);
        }
        Collections.shuffle(playersList);
        int teamSize = Math.max(1, this.mode.getTeam1Size());
        int teamCount = 1;
        for (int i = 0; i < playersList.size(); i += teamSize) {
            ArrayList<Player> teamMembers = new ArrayList<Player>();
            for (int j = 0; j < teamSize && i + j < playersList.size(); ++j) {
                teamMembers.add(playersList.get(i + j));
            }
            DuelTeam team = new DuelTeam("Equipo " + teamCount++, teamMembers);
            this.activeTeams.add(team);
        }
        this.runBracketRound();
    }

    private void runBracketRound() {
        if (this.activeTeams.size() <= 1) {
            this.finishEvent();
            return;
        }
        final List<DuelTeam> winnersOfRound = Collections.synchronizedList(new ArrayList<DuelTeam>());
        final int expectedWinners = (this.activeTeams.size() + 1) / 2;

        for (int i = 0; i < this.activeTeams.size(); i += 2) {
            if (i + 1 < this.activeTeams.size()) {
                DuelTeam t1 = this.activeTeams.get(i);
                DuelTeam t2 = this.activeTeams.get(i + 1);
                Arena arena = this.plugin.getArenaManager().getAvailableArena();
                if (arena != null) {
                    ++this.totalMatches;
                    final DuelMatch match = new DuelMatch(this.plugin, arena, this.kit, this.mode, t1, t2, null);
                    this.eventMatches.add(match);
                    this.plugin.getDuelManager().registerMatch(match);
                    match.startMatchSequence();
                    new BukkitRunnable(){

                        public void run() {
                            if (DuelEvent.this.state == EventState.FINISHED) {
                                this.cancel();
                                return;
                            }
                            if (match.isFinished()) {
                                this.cancel();
                                DuelTeam roundWinner = match.getWinner();
                                if (roundWinner != null) {
                                    winnersOfRound.add(roundWinner);
                                } else {
                                    winnersOfRound.add(t1); // Default winner fallback on tie
                                }
                            }
                        }
                    }.runTaskTimer((Plugin)this.plugin, 40L, 20L);
                    continue;
                }
                winnersOfRound.add(t1);
                continue;
            }
            winnersOfRound.add(this.activeTeams.get(i));
        }

        new BukkitRunnable(){

            public void run() {
                if (DuelEvent.this.state == EventState.FINISHED) {
                    this.cancel();
                    return;
                }
                if (winnersOfRound.size() >= expectedWinners) {
                    this.cancel();
                    DuelEvent.this.activeTeams.clear();
                    DuelEvent.this.activeTeams.addAll(winnersOfRound);
                    new BukkitRunnable() {
                        public void run() {
                            if (DuelEvent.this.state != EventState.FINISHED) {
                                DuelEvent.this.runBracketRound();
                            }
                        }
                    }.runTaskLater((Plugin)DuelEvent.this.plugin, 40L);
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 40L, 20L);
    }

    private void finishEvent() {
        this.state = EventState.FINISHED;
        this.endTime = System.currentTimeMillis();
        if (!this.activeTeams.isEmpty()) {
            DuelTeam winningTeam = this.activeTeams.get(0);
            this.winnerTeamName = winningTeam.getName();
            this.winnerName = winningTeam.getFormattedMembers();
            for (Player p : winningTeam.getOnlinePlayers()) {
                this.plugin.getDuelEventManager().queueRewardsForDelivery(p, this.randomRewards);
            }
        }
        this.announceCompletion();
    }

    public void cancelDueToTimeout() {
        if (this.state == EventState.FINISHED) {
            return;
        }
        this.state = EventState.FINISHED;
        this.endTime = System.currentTimeMillis();
        Bukkit.broadcast((Component)TextUtil.toComponent("&cEl Evento de Duelos ha finalizado porque alcanzo el limite de tiempo."));
        for (DuelMatch match : new ArrayList<DuelMatch>(this.eventMatches)) {
            if (match.isFinished()) continue;
            match.endMatch(null);
        }
    }

    public void announceCompletion() {
        List<String> broadcastTemplate = this.plugin.getConfig().getStringList("event.end_broadcast");
        if (broadcastTemplate == null || broadcastTemplate.isEmpty()) {
            broadcastTemplate = Arrays.asList("-------------------------------------------", "              EVENTO DE DUELOS", "", "\t El duelo %spazioduels_event% ha finalizado.", "", "\t Los ganadores del evento son:", "\t %spazioduels_winer%/%spazioduels_winer_team%", "", "\t Recompensas recibidas:", "", "\t - %spazioduels_randomreward%", "\t - %spazioduels_randomreward%", "\t - %spazioduels_randomreward%", "", "\t - CLICK PARA VER LA INFORMACION -", "-------------------------------------------");
        }
        int rewardIdx = 0;
        for (String rawLine : broadcastTemplate) {
            String line = rawLine.replace("%spazioduels_event%", this.eventName).replace("%spazioduels_winer%", this.winnerName).replace("%spazioduels_winer_team%", this.winnerTeamName);
            while (line.contains("%spazioduels_randomreward%")) {
                String rewardName = rewardIdx < this.randomRewards.size() ? this.randomRewards.get(rewardIdx++).getName() : "&7Recompensa sorpresa";
                line = line.replaceFirst("%spazioduels_randomreward%", Matcher.quoteReplacement(rewardName));
            }
            if (line.contains("- CLICK PARA VER LA INFORMACION -")) {
                Component clickComponent = TextUtil.createClickableComponent(line, "/duelevent summary " + this.eventId.toString(), "&e\u00a1Haz clic para ver el resumen del evento!");
                Bukkit.broadcast((Component)clickComponent);
                continue;
            }
            Bukkit.broadcast((Component)TextUtil.toComponent(line));
        }
        EventSummary summary = new EventSummary(this.eventId, this.eventName, this.mode.getDisplayName(), this.kit != null ? this.kit.getName() : "Default", this.winnerName, this.winnerTeamName, this.randomRewards, (this.endTime - this.startTime) / 1000L, this.totalMatches, this.totalKills);
        this.plugin.getDuelEventManager().registerSummary(summary);
    }

    public UUID getEventId() {
        return this.eventId;
    }

    public String getEventName() {
        return this.eventName;
    }

    public DuelMode getMode() {
        return this.mode;
    }

    public Kit getKit() {
        return this.kit;
    }

    public EventState getState() {
        return this.state;
    }

    public List<RewardUtil.RewardItem> getRandomRewards() {
        return this.randomRewards;
    }
}

