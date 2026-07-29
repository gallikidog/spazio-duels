package network.minespazio.spazioduels.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothMatch;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;

public class ScoreboardManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, SpazioBoard> activeBoards = new HashMap<>();
    private File file;
    private FileConfiguration config;
    private BukkitTask task;
    private boolean papiEnabled;

    public ScoreboardManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        loadConfig();
        startUpdateTask();
    }

    public void loadConfig() {
        file = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!file.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reloadConfig() {
        loadConfig();
        if (task != null) {
            task.cancel();
        }
        startUpdateTask();
    }

    private void startUpdateTask() {
        if (!config.getBoolean("scoreboard.enabled", true)) {
            clearAllBoards();
            return;
        }

        int updateTicks = Math.max(1, config.getInt("scoreboard.update_ticks", 2));

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, updateTicks);
    }

    private void tick() {
        Set<UUID> onlineUuids = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            onlineUuids.add(player.getUniqueId());
            updatePlayerScoreboard(player);
        }

        // Clean up boards for disconnected players
        Iterator<Map.Entry<UUID, SpazioBoard>> iterator = activeBoards.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SpazioBoard> entry = iterator.next();
            if (!onlineUuids.contains(entry.getKey())) {
                entry.getValue().destroy();
                iterator.remove();
            }
        }
    }

    private void updatePlayerScoreboard(Player player) {
        DuelMatch duelMatch = plugin.getDuelManager().getMatch(player);

        if (duelMatch != null) {
            // Player is in a duel
            renderDuelScoreboard(player, duelMatch);
            return;
        }

        // Check if player is in an active KOTH zone
        KothMatch kothMatch = plugin.getKothManager().getActiveMatch();
        if (kothMatch != null && kothMatch.isActive()) {
            Koth koth = kothMatch.getKoth();
            CuboidRegion activeZone = koth.getZone() != null ? koth.getZone() : koth.getCapZone();
            if (activeZone != null && activeZone.contains(player.getLocation())) {
                renderKothScoreboard(player, kothMatch);
                return;
            }
        }

        // If player is neither in a duel nor in an active KOTH zone, remove custom scoreboard
        removeBoard(player);
    }

    private void renderKothScoreboard(Player player, KothMatch match) {
        String titleRaw = config.getString("scoreboard.koth.title", "&6&lKing Of The Hill");
        List<String> rawLines = config.getStringList("scoreboard.koth.lines");

        String capperNone = config.getString("scoreboard.koth.capper_none", "&c&lNadie");
        String statusCapturing = config.getString("scoreboard.koth.status_capturing", "&aCapturando...");
        String statusWaiting = config.getString("scoreboard.koth.status_waiting", "&cSin capturar");

        String kothName = match.getKoth().getName();
        Player capper = match.getCurrentCapper();
        String capperName = capper != null ? capper.getName() : capperNone;
        String status = capper != null ? statusCapturing : statusWaiting;
        String timeSec = match.formatTime(match.getRemainingSeconds());
        String timeMs = match.formatTimeMillis();

        List<String> formattedLines = new ArrayList<>();
        for (String line : rawLines) {
            String formatted = line
                    .replace("%koth_name%", kothName)
                    .replace("%koth_status%", status)
                    .replace("%koth_capper%", capperName)
                    .replace("%koth_time%", timeSec)
                    .replace("%koth_time_ms%", timeMs);

            if (papiEnabled) {
                formatted = PlaceholderAPI.setPlaceholders(player, formatted);
            }
            formattedLines.add(TextUtil.colorize(formatted));
        }

        String title = TextUtil.colorize(papiEnabled ? PlaceholderAPI.setPlaceholders(player, titleRaw) : titleRaw);
        getOrCreateBoard(player, title).update(title, formattedLines);
    }

    private void renderDuelScoreboard(Player player, DuelMatch match) {
        DuelTeam playerTeam = match.getTeamOf(player);
        if (playerTeam == null) {
            removeBoard(player);
            return;
        }

        DuelTeam enemyTeam = match.getTeam1().equals(playerTeam) ? match.getTeam2() : match.getTeam1();

        // 1v1 Solo Mode vs Teams Mode
        boolean isSolo = playerTeam.getMembers().size() == 1 && enemyTeam.getMembers().size() == 1;

        if (isSolo) {
            renderDuelSoloScoreboard(player, match, playerTeam, enemyTeam);
        } else {
            renderDuelTeamScoreboard(player, match, playerTeam, enemyTeam);
        }
    }

    private void renderDuelSoloScoreboard(Player player, DuelMatch match, DuelTeam playerTeam, DuelTeam enemyTeam) {
        String titleRaw = config.getString("scoreboard.duel_solo.title", "&b&lDuelo 1v1");
        List<String> rawLines = config.getStringList("scoreboard.duel_solo.lines");

        UUID opponentUuid = enemyTeam.getMembers().get(0);
        Player opponent = Bukkit.getPlayer(opponentUuid);

        String playerName = player.getName();
        String playerHealth = String.format("%.1f", player.getHealth());
        int playerHits = playerTeam.getHits(player.getUniqueId());

        String opponentName = opponent != null ? opponent.getName() : "Rival";
        String opponentHealth = opponent != null && opponent.isOnline() && !opponent.isDead() ? String.format("%.1f", opponent.getHealth()) : "0.0";
        int opponentHits = enemyTeam.getHits(opponentUuid);

        long durationSecs = match.getDurationSeconds();
        String timeFormatted = String.format("%02d:%02d", durationSecs / 60, durationSecs % 60);

        List<String> formattedLines = new ArrayList<>();
        for (String line : rawLines) {
            String formatted = line
                    .replace("%player_name%", playerName)
                    .replace("%player_health%", playerHealth)
                    .replace("%player_hits%", String.valueOf(playerHits))
                    .replace("%opponent_name%", opponentName)
                    .replace("%opponent_health%", opponentHealth)
                    .replace("%opponent_hits%", String.valueOf(opponentHits))
                    .replace("%duel_time%", timeFormatted);

            if (papiEnabled) {
                formatted = PlaceholderAPI.setPlaceholders(player, formatted);
            }
            formattedLines.add(TextUtil.colorize(formatted));
        }

        String title = TextUtil.colorize(papiEnabled ? PlaceholderAPI.setPlaceholders(player, titleRaw) : titleRaw);
        getOrCreateBoard(player, title).update(title, formattedLines);
    }

    private void renderDuelTeamScoreboard(Player player, DuelMatch match, DuelTeam playerTeam, DuelTeam enemyTeam) {
        String titleRaw = config.getString("scoreboard.duel_team.title", "&b&lDuelo de Equipos");
        List<String> rawLines = config.getStringList("scoreboard.duel_team.lines");
        String memberAliveFmt = config.getString("scoreboard.duel_team.member_line_alive", " &7%player_name% &f(%health% ❤ - %hits% hits)");
        String memberDeadFmt = config.getString("scoreboard.duel_team.member_line_dead", " &7%player_name% &c(MUERTO)");

        long durationSecs = match.getDurationSeconds();
        String timeFormatted = String.format("%02d:%02d", durationSecs / 60, durationSecs % 60);

        // Build player team member lines
        List<String> yourTeamLines = buildTeamMemberLines(playerTeam, memberAliveFmt, memberDeadFmt);
        // Build enemy team member lines
        List<String> enemyTeamLines = buildTeamMemberLines(enemyTeam, memberAliveFmt, memberDeadFmt);

        List<String> formattedLines = new ArrayList<>();
        for (String line : rawLines) {
            if (line.contains("%your_team_members%")) {
                formattedLines.addAll(yourTeamLines);
            } else if (line.contains("%enemy_team_members%")) {
                formattedLines.addAll(enemyTeamLines);
            } else {
                String formatted = line.replace("%duel_time%", timeFormatted);
                if (papiEnabled) {
                    formatted = PlaceholderAPI.setPlaceholders(player, formatted);
                }
                formattedLines.add(TextUtil.colorize(formatted));
            }
        }

        String title = TextUtil.colorize(papiEnabled ? PlaceholderAPI.setPlaceholders(player, titleRaw) : titleRaw);
        getOrCreateBoard(player, title).update(title, formattedLines);
    }

    private List<String> buildTeamMemberLines(DuelTeam team, String aliveFmt, String deadFmt) {
        List<String> lines = new ArrayList<>();
        for (UUID uuid : team.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            String name = member != null ? member.getName() : "Desconocido";

            if (team.getAliveMembers().contains(uuid) && member != null && member.isOnline()) {
                String hp = String.format("%.1f", member.getHealth());
                int hits = team.getHits(uuid);
                String line = aliveFmt
                        .replace("%player_name%", name)
                        .replace("%health%", hp)
                        .replace("%hits%", String.valueOf(hits));
                lines.add(TextUtil.colorize(line));
            } else {
                String line = deadFmt.replace("%player_name%", name);
                lines.add(TextUtil.colorize(line));
            }
        }
        return lines;
    }

    private SpazioBoard getOrCreateBoard(Player player, String title) {
        return activeBoards.computeIfAbsent(player.getUniqueId(), uuid -> new SpazioBoard(player, title));
    }

    public void removeBoard(Player player) {
        SpazioBoard board = activeBoards.remove(player.getUniqueId());
        if (board != null) {
            board.destroy();
        }
    }

    public void clearAllBoards() {
        for (SpazioBoard board : activeBoards.values()) {
            board.destroy();
        }
        activeBoards.clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
