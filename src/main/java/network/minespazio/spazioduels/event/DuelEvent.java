package network.minespazio.spazioduels.event;

import net.kyori.adventure.text.Component;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.RewardUtil;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelEvent {

    private final UUID eventId;
    private final SpazioDuelsPlugin plugin;
    private final String eventName;
    private final DuelMode mode;
    private final Kit kit;
    private final List<RewardUtil.RewardItem> randomRewards;
    private EventState state;

    private final Set<UUID> registeredPlayers = new LinkedHashSet<>();
    private final List<DuelTeam> activeTeams = new ArrayList<>();
    private final long startTime;
    private long endTime;

    private String winnerName = "Ninguno";
    private String winnerTeamName = "Sin Equipo";
    private int totalMatches = 0;
    private int totalKills = 0;

    public DuelEvent(SpazioDuelsPlugin plugin, String eventName, DuelMode mode, Kit kit, List<RewardUtil.RewardItem> rewardsPool) {
        this.eventId = UUID.randomUUID();
        this.plugin = plugin;
        this.eventName = eventName;
        this.mode = mode;
        this.kit = kit;
        this.state = EventState.WAITING_PLAYERS;
        this.startTime = System.currentTimeMillis();

        this.randomRewards = RewardUtil.getRandomRewards(rewardsPool, 3);
    }

    public void announceStart() {
        List<String> broadcastTemplate = plugin.getConfig().getStringList("event.start_broadcast");
        if (broadcastTemplate == null || broadcastTemplate.isEmpty()) {
            broadcastTemplate = Arrays.asList(
                    "-------------------------------------------",
                    "              EVENTO DE DUELOS",
                    "",
                    "	 Un evento de duelos iniciara en breves.",
                    "",
                    "	 Modo seleccionado: %spazioduels_mode%",
                    "	 Kit seleccionado: %spazioduels_kitselect%",
                    "",
                    "	 Recompensa:",
                    "	 - %spazioduels_randomreward%",
                    "	 - %spazioduels_randomreward%",
                    "	 - %spazioduels_randomreward%",
                    "",
                    "	 - CLICK PARA INGRESAR AL EVENTO -",
                    "-------------------------------------------"
            );
        }

        int rewardIdx = 0;

        for (String rawLine : broadcastTemplate) {
            String line = rawLine.replace("%spazioduels_mode%", mode.getDisplayName())
                                 .replace("%spazioduels_kitselect%", kit != null ? kit.getName() : "Standard")
                                 .replace("%spazioduels_event%", eventName);

            while (line.contains("%spazioduels_randomreward%")) {
                String rewardName = (rewardIdx < randomRewards.size()) ? randomRewards.get(rewardIdx++).getName() : "&7Recompensa sorpresa";
                line = line.replaceFirst("%spazioduels_randomreward%", rewardName);
            }

            if (line.contains("- CLICK PARA INGRESAR AL EVENTO -")) {
                Component clickComponent = TextUtil.createClickableComponent(
                        line,
                        "/duelevent join",
                        "&a¡Haz clic para unirte al Evento de Duelos!"
                );
                Bukkit.broadcast(clickComponent);
            } else {
                Bukkit.broadcast(TextUtil.toComponent(line));
            }
        }
    }

    public boolean registerPlayer(Player player) {
        if (state != EventState.WAITING_PLAYERS) {
            player.sendMessage(TextUtil.colorize("&cEl evento ya ha comenzado o finalizado."));
            return false;
        }

        if (registeredPlayers.contains(player.getUniqueId())) {
            player.sendMessage(TextUtil.colorize("&cYa estás registrado en este evento."));
            return false;
        }

        registeredPlayers.add(player.getUniqueId());
        player.sendMessage(TextUtil.colorize("&a¡Te has unido exitosamente al evento de duelos!"));
        return true;
    }

    public void startTournament() {
        if (registeredPlayers.size() < 2) {
            Bukkit.broadcast(TextUtil.toComponent("&cEl Evento de Duelos ha sido cancelado por falta de participantes."));
            state = EventState.FINISHED;
            return;
        }

        state = EventState.RUNNING_TOURNAMENT;
        Bukkit.broadcast(TextUtil.toComponent("&a¡El Evento de Duelos ha comenzado con &e" + registeredPlayers.size() + " &ajugadores!"));

        // Form teams according to mode
        List<Player> playersList = new ArrayList<>();
        for (UUID uuid : registeredPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                playersList.add(p);
            }
        }

        Collections.shuffle(playersList);
        int teamSize = Math.max(1, mode.getTeam1Size());
        int teamCount = 1;

        for (int i = 0; i < playersList.size(); i += teamSize) {
            List<Player> teamMembers = new ArrayList<>();
            for (int j = 0; j < teamSize && (i + j) < playersList.size(); j++) {
                teamMembers.add(playersList.get(i + j));
            }
            DuelTeam team = new DuelTeam("Equipo " + teamCount++, teamMembers);
            activeTeams.add(team);
        }

        runBracketRound();
    }

    private void runBracketRound() {
        if (activeTeams.size() <= 1) {
            finishEvent();
            return;
        }

        List<DuelTeam> winnersOfRound = Collections.synchronizedList(new ArrayList<>());
        List<BukkitRunnable> roundMatches = new ArrayList<>();

        for (int i = 0; i < activeTeams.size(); i += 2) {
            if (i + 1 < activeTeams.size()) {
                DuelTeam t1 = activeTeams.get(i);
                DuelTeam t2 = activeTeams.get(i + 1);

                network.minespazio.spazioduels.arena.Arena arena = plugin.getArenaManager().getAvailableArena();
                if (arena != null) {
                    totalMatches++;
                    DuelMatch match = new DuelMatch(plugin, arena, kit, mode, t1, t2, null);
                    plugin.getDuelManager().registerMatch(match);
                    match.startMatchSequence();

                    // Wait for match finish task
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (match.isFinished()) {
                                cancel();
                                DuelTeam roundWinner = match.getWinner();
                                if (roundWinner != null) {
                                    winnersOfRound.add(roundWinner);
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 40L, 20L);
                } else {
                    // Fallback pass if no arena available
                    winnersOfRound.add(t1);
                }
            } else {
                // Odd team gets a bye to next round
                winnersOfRound.add(activeTeams.get(i));
            }
        }

        // Wait for all matches in round to complete before proceeding to next round
        new BukkitRunnable() {
            @Override
            public void run() {
                if (winnersOfRound.size() >= activeTeams.size() / 2) {
                    cancel();
                    activeTeams.clear();
                    activeTeams.addAll(winnersOfRound);
                    runBracketRound();
                }
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    private void finishEvent() {
        state = EventState.FINISHED;
        endTime = System.currentTimeMillis();

        if (!activeTeams.isEmpty()) {
            DuelTeam winningTeam = activeTeams.get(0);
            winnerTeamName = winningTeam.getName();
            winnerName = winningTeam.getFormattedMembers();

            // Give rewards to winners
            for (Player p : winningTeam.getOnlinePlayers()) {
                RewardUtil.executeRewards(p, randomRewards);
            }
        }

        announceCompletion();
    }

    public void announceCompletion() {
        List<String> broadcastTemplate = plugin.getConfig().getStringList("event.end_broadcast");
        if (broadcastTemplate == null || broadcastTemplate.isEmpty()) {
            broadcastTemplate = Arrays.asList(
                    "-------------------------------------------",
                    "              EVENTO DE DUELOS",
                    "",
                    "	 El duelo %spazioduels_event% ha finalizado.",
                    "",
                    "	 Los ganadores del evento son:",
                    "	 %spazioduels_winer%/%spazioduels_winer_team%",
                    "",
                    "	 Recompensas recibidas:",
                    "",
                    "	 - %spazioduels_randomreward%",
                    "	 - %spazioduels_randomreward%",
                    "	 - %spazioduels_randomreward%",
                    "",
                    "	 - CLICK PARA VER LA INFORMACION -",
                    "-------------------------------------------"
            );
        }

        int rewardIdx = 0;

        for (String rawLine : broadcastTemplate) {
            String line = rawLine.replace("%spazioduels_event%", eventName)
                                 .replace("%spazioduels_winer%", winnerName)
                                 .replace("%spazioduels_winer_team%", winnerTeamName);

            while (line.contains("%spazioduels_randomreward%")) {
                String rewardName = (rewardIdx < randomRewards.size()) ? randomRewards.get(rewardIdx++).getName() : "&7Recompensa sorpresa";
                line = line.replaceFirst("%spazioduels_randomreward%", rewardName);
            }

            if (line.contains("- CLICK PARA VER LA INFORMACION -")) {
                Component clickComponent = TextUtil.createClickableComponent(
                        line,
                        "/duelevent summary " + eventId.toString(),
                        "&e¡Haz clic para ver el resumen del evento!"
                );
                Bukkit.broadcast(clickComponent);
            } else {
                Bukkit.broadcast(TextUtil.toComponent(line));
            }
        }

        // Store summary in DuelEventManager
        EventSummary summary = new EventSummary(
                eventId,
                eventName,
                mode.getDisplayName(),
                kit != null ? kit.getName() : "Default",
                winnerName,
                winnerTeamName,
                randomRewards,
                (endTime - startTime) / 1000,
                totalMatches,
                totalKills
        );
        plugin.getDuelEventManager().registerSummary(summary);
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public DuelMode getMode() {
        return mode;
    }

    public Kit getKit() {
        return kit;
    }

    public EventState getState() {
        return state;
    }

    public List<RewardUtil.RewardItem> getRandomRewards() {
        return randomRewards;
    }
}
