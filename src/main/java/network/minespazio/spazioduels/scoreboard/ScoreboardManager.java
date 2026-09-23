/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  network.minespazio.coresurvival.api.module.ModuleId
 *  network.minespazio.coresurvival.board.api.BoardChannel
 *  network.minespazio.coresurvival.board.api.BoardLease
 *  network.minespazio.coresurvival.board.api.BoardPriority
 *  network.minespazio.coresurvival.board.api.BoardRequest
 *  network.minespazio.coresurvival.board.api.BoardService
 *  network.minespazio.coresurvival.board.api.BoardView
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredServiceProvider
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package network.minespazio.spazioduels.scoreboard;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import network.minespazio.coresurvival.api.module.ModuleId;
import network.minespazio.coresurvival.board.api.BoardChannel;
import network.minespazio.coresurvival.board.api.BoardLease;
import network.minespazio.coresurvival.board.api.BoardPriority;
import network.minespazio.coresurvival.board.api.BoardRequest;
import network.minespazio.coresurvival.board.api.BoardService;
import network.minespazio.coresurvival.board.api.BoardView;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.event.DuelEvent;
import network.minespazio.spazioduels.event.EventState;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothMatch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class ScoreboardManager
implements Listener {
    private static final ModuleId PROVIDER = ModuleId.of((String)"spazio-duels");
    private static final int DEFAULT_RECONCILIATION_TICKS = 20;
    private static final int DEFAULT_ACTIVE_UPDATE_TICKS = 5;
    private static final int MIN_ACTIVE_UPDATE_TICKS = 2;
    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, BoardLease> activeLeases = new HashMap<UUID, BoardLease>();
    private final Map<UUID, Long> generations = new HashMap<UUID, Long>();
    private final Map<UUID, PublishedBoardState> lastPublished = new HashMap<UUID, PublishedBoardState>();
    private final BoardService boardService;
    private File file;
    private FileConfiguration config;
    private BukkitTask reconciliationTask;
    private BukkitTask activeTask;
    private boolean listenerRegistered;

    public ScoreboardManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        RegisteredServiceProvider registration = Bukkit.getServicesManager().getRegistration(BoardService.class);
        this.boardService = registration == null ? null : (BoardService)registration.getProvider();
        this.loadConfig();
        this.startUpdateTask();
        if (this.boardService == null) {
            this.plugin.getLogger().warning("BoardService no esta disponible; SpazioDuels no publicara scoreboard.");
        }
    }

    public void loadConfig() {
        this.file = new File(this.plugin.getDataFolder(), "scoreboard.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("scoreboard.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void reloadConfig() {
        this.loadConfig();
        this.cancelTasks();
        if (!this.config.getBoolean("scoreboard.enabled", true)) {
            this.clearAllBoards();
            return;
        }
        this.startUpdateTask();
    }

    private void startUpdateTask() {
        if (this.boardService == null) {
            return;
        }
        if (!this.config.getBoolean("scoreboard.enabled", true)) {
            return;
        }
        this.registerListener();
        int reconciliationTicks = Math.max(20, this.config.getInt("scoreboard.reconciliation_ticks", 20));
        int activeUpdateTicks = Math.max(2, this.config.getInt("scoreboard.active_update_ticks", this.config.getInt("scoreboard.update_ticks", 5)));
        this.reconciliationTask = new BukkitRunnable(){

            public void run() {
                ScoreboardManager.this.tickAllPlayers();
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, (long)reconciliationTicks);
        this.activeTask = new BukkitRunnable(){

            public void run() {
                ScoreboardManager.this.tickActivePlayers();
            }
        }.runTaskTimer((Plugin)this.plugin, (long)activeUpdateTicks, (long)activeUpdateTicks);
    }

    private void registerListener() {
        if (this.listenerRegistered) {
            return;
        }
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        this.listenerRegistered = true;
    }

    private void unregisterListener() {
        if (!this.listenerRegistered) {
            return;
        }
        HandlerList.unregisterAll((Listener)this);
        this.listenerRegistered = false;
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.boardService == null || !this.config.getBoolean("scoreboard.enabled", true)) {
            return;
        }
        if (event.getTo() == null || ScoreboardManager.sameBlock(event)) {
            return;
        }
        this.updatePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.removeBoard(event.getPlayer());
    }

    private void tickAllPlayers() {
        HashSet<UUID> online = new HashSet<UUID>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            online.add(player.getUniqueId());
            this.updatePlayer(player);
        }
        Iterator<Map.Entry<UUID, BoardLease>> iterator = this.activeLeases.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BoardLease> entry = iterator.next();
            if (online.contains(entry.getKey())) continue;
            entry.getValue().release();
            this.generations.remove(entry.getKey());
            this.lastPublished.remove(entry.getKey());
            iterator.remove();
        }
    }

    private void tickActivePlayers() {
        for (UUID playerId : List.copyOf(this.activeLeases.keySet())) {
            Player player = Bukkit.getPlayer((UUID)playerId);
            if (player == null || !player.isOnline()) {
                this.removeBoard(playerId);
                continue;
            }
            this.updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        DuelEvent event;
        DuelMatch duel = this.plugin.getDuelManager().getMatch(player);
        if (duel != null) {
            this.publishDuel(player, duel);
            return;
        }
        KothMatch kothMatch = this.plugin.getKothManager().getActiveMatch();
        if (kothMatch != null && kothMatch.isActive()) {
            CuboidRegion zone;
            Koth koth = kothMatch.getKoth();
            CuboidRegion cuboidRegion = zone = koth.getZone() != null ? koth.getZone() : koth.getCapZone();
            if (zone != null && zone.contains(player.getLocation())) {
                this.publishKoth(player, kothMatch);
                return;
            }
            if (this.config.getBoolean("scoreboard.show_global_events", true)) {
                this.publishKothEvent(player, kothMatch);
                return;
            }
        }
        if ((event = this.plugin.getDuelEventManager().getActiveEvent()) != null && event.getState() != EventState.FINISHED && this.config.getBoolean("scoreboard.show_global_events", true)) {
            this.publishDuelEvent(player, event);
            return;
        }
        this.removeBoard(player);
    }

    private void publishDuel(Player player, DuelMatch match) {
        DuelTeam ownTeam = match.getTeamOf(player);
        if (ownTeam == null) {
            this.removeBoard(player);
            return;
        }
        DuelTeam enemyTeam = match.getTeam1().equals(ownTeam) ? match.getTeam2() : match.getTeam1();
        HashMap<String, String> fields = new HashMap<String, String>();
        fields.put("player_name", player.getName());
        fields.put("player_health", ScoreboardManager.formatHealth(player));
        fields.put("player_hits", String.valueOf(ownTeam.getHits(player.getUniqueId())));
        fields.put("duel_time", ScoreboardManager.formatDuration(match.getDurationSeconds()));
        fields.put("mode", ownTeam.getMembers().size() == 1 && enemyTeam.getMembers().size() == 1 ? "solo" : "team");
        if ("solo".equals(fields.get("mode"))) {
            UUID opponentId = enemyTeam.getMembers().get(0);
            Player opponent = Bukkit.getPlayer((UUID)opponentId);
            fields.put("opponent_name", opponent == null ? "Rival" : opponent.getName());
            fields.put("opponent_health", ScoreboardManager.formatHealth(opponent));
            fields.put("opponent_hits", String.valueOf(enemyTeam.getHits(opponentId)));
        } else {
            fields.put("your_team_members", ScoreboardManager.teamMembers(ownTeam));
            fields.put("enemy_team_members", ScoreboardManager.teamMembers(enemyTeam));
        }
        this.publish(player, "duel-active", BoardPriority.DUEL_ACTIVE, fields);
    }

    private void publishKoth(Player player, KothMatch match) {
        Player capper = match.getCurrentCapper();
        HashMap<String, String> fields = new HashMap<String, String>();
        fields.put("koth_name", match.getKoth().getName());
        fields.put("koth_capper", capper == null ? "Nadie" : capper.getName());
        fields.put("koth_status", capper == null ? "Sin capturar" : "Capturando...");
        fields.put("koth_time", match.formatTime(match.getRemainingSeconds()));
        fields.put("koth_time_ms", match.formatTimeMillis());
        this.publish(player, "koth-zone", BoardPriority.KOTH_ZONE, fields);
    }

    private void publishKothEvent(Player player, KothMatch match) {
        HashMap<String, String> fields = new HashMap<String, String>();
        Player capper = match.getCurrentCapper();
        fields.put("event_type", "KOTH");
        fields.put("event_name", match.getKoth().getName());
        fields.put("event_status", (String)(capper == null ? "En disputa" : "Captura: " + capper.getName()));
        fields.put("event_mode", "Zona activa");
        fields.put("event_kit", "Recompensa KOTH");
        this.publish(player, "spazioduels-event", BoardPriority.EVENT_SPECTATOR, fields);
    }

    private void publishDuelEvent(Player player, DuelEvent event) {
        HashMap<String, String> fields = new HashMap<String, String>();
        Kit kit = event.getKit();
        fields.put("event_type", "Duelos");
        fields.put("event_name", event.getEventName());
        fields.put("event_status", ScoreboardManager.eventStatus(event.getState()));
        fields.put("event_mode", event.getMode().getDisplayName());
        fields.put("event_kit", kit == null ? "Default" : kit.getName());
        this.publish(player, "spazioduels-event", BoardPriority.EVENT_SPECTATOR, fields);
    }

    private void publish(Player player, String templateId, BoardPriority priority, Map<String, String> fields) {
        UUID playerId = player.getUniqueId();
        Map stableFields = Map.copyOf(fields);
        PublishedBoardState nextState = new PublishedBoardState(templateId, priority, stableFields);
        BoardLease current = this.activeLeases.get(playerId);
        if (current != null && current.isActive() && current.snapshot().priority() == priority) {
            if (nextState.equals(this.lastPublished.get(playerId))) {
                return;
            }
            long generation = this.generations.merge(playerId, 1L, Long::sum);
            current.update(new BoardView(templateId, stableFields, Set.copyOf(stableFields.keySet()), generation));
            this.lastPublished.put(playerId, nextState);
            return;
        }
        if (current != null) {
            current.release();
        }
        long generation = this.generations.merge(playerId, 1L, Long::sum);
        BoardRequest request = new BoardRequest(playerId, PROVIDER, BoardChannel.SIDEBAR, priority, generation, templateId, null);
        BoardLease lease = this.boardService.acquire(request);
        lease.update(new BoardView(templateId, stableFields, Set.copyOf(stableFields.keySet()), generation));
        this.activeLeases.put(playerId, lease);
        this.lastPublished.put(playerId, nextState);
    }

    public void removeBoard(Player player) {
        this.removeBoard(player.getUniqueId());
    }

    private void removeBoard(UUID playerId) {
        BoardLease lease = this.activeLeases.remove(playerId);
        if (lease != null) {
            lease.release();
        }
        this.generations.remove(playerId);
        this.lastPublished.remove(playerId);
    }

    public void clearAllBoards() {
        this.cancelTasks();
        for (BoardLease lease : this.activeLeases.values()) {
            lease.release();
        }
        this.activeLeases.clear();
        this.generations.clear();
        this.lastPublished.clear();
        if (this.boardService != null) {
            this.boardService.releaseProvider(PROVIDER);
        }
        this.unregisterListener();
    }

    private void cancelTasks() {
        if (this.reconciliationTask != null) {
            this.reconciliationTask.cancel();
            this.reconciliationTask = null;
        }
        if (this.activeTask != null) {
            this.activeTask.cancel();
            this.activeTask = null;
        }
    }

    private static String formatHealth(Player player) {
        return player == null || !player.isOnline() || player.isDead() ? "0.0" : String.format("%.1f", player.getHealth());
    }

    private static String formatDuration(long seconds) {
        return String.format("%02d:%02d", seconds / 60L, seconds % 60L);
    }

    private static String eventStatus(EventState state) {
        if (state == EventState.WAITING_PLAYERS) {
            return "Esperando jugadores";
        }
        if (state == EventState.RUNNING_TOURNAMENT) {
            return "Torneo activo";
        }
        return "Finalizado";
    }

    private static boolean sameBlock(PlayerMoveEvent event) {
        return event.getFrom().getWorld().equals((Object)event.getTo().getWorld()) && event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ();
    }

    private static String teamMembers(DuelTeam team) {
        StringBuilder result = new StringBuilder();
        for (UUID memberId : team.getMembers()) {
            String name;
            Player member = Bukkit.getPlayer((UUID)memberId);
            String string = name = member == null ? "Desconocido" : member.getName();
            if (result.length() > 0) {
                result.append("\n");
            }
            if (team.getAliveMembers().contains(memberId) && member != null && member.isOnline()) {
                result.append(name).append("|").append(ScoreboardManager.formatHealth(member)).append("|").append(team.getHits(memberId));
                continue;
            }
            result.append(name).append("|MUERTO");
        }
        return result.toString();
    }

    private static final class PublishedBoardState {
        private final String templateId;
        private final BoardPriority priority;
        private final Map<String, String> fields;

        private PublishedBoardState(String templateId, BoardPriority priority, Map<String, String> fields) {
            this.templateId = templateId;
            this.priority = priority;
            this.fields = fields;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PublishedBoardState)) {
                return false;
            }
            PublishedBoardState other = (PublishedBoardState)obj;
            return this.templateId.equals(other.templateId) && this.priority == other.priority && this.fields.equals(other.fields);
        }

        public int hashCode() {
            int result = this.templateId.hashCode();
            result = 31 * result + this.priority.hashCode();
            result = 31 * result + this.fields.hashCode();
            return result;
        }
    }
}

