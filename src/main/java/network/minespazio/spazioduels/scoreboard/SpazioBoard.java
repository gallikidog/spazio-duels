package network.minespazio.spazioduels.scoreboard;

import net.kyori.adventure.text.Component;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class SpazioBoard {

    private final Player player;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<Team> lineTeams = new ArrayList<>();
    private final List<String> entryNames = new ArrayList<>();
    private String currentTitle = "";
    private final List<String> currentLines = new ArrayList<>();

    private static final String[] COLOR_ENTRIES = {
            "§0§r", "§1§r", "§2§r", "§3§r", "§4§r", "§5§r", "§6§r", "§7§r",
            "§8§r", "§9§r", "§a§r", "§b§r", "§c§r", "§d§r", "§e§r"
    };

    public SpazioBoard(Player player, String initialTitle) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        Objective obj = scoreboard.registerNewObjective("spazioduels", Criteria.DUMMY, TextUtil.toComponent(initialTitle));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective = obj;
        this.currentTitle = initialTitle;

        // Initialize up to 15 team lines
        for (int i = 0; i < 15; i++) {
            String teamName = "line_" + i;
            Team team = scoreboard.registerNewTeam(teamName);
            String entry = COLOR_ENTRIES[i];
            team.addEntry(entry);
            lineTeams.add(team);
            entryNames.add(entry);
        }

        // Assign scoreboard to player
        player.setScoreboard(scoreboard);
    }

    public void update(String title, List<String> lines) {
        if (!player.isOnline()) return;

        // Update Title if changed
        if (!title.equals(currentTitle)) {
            currentTitle = title;
            objective.displayName(TextUtil.toComponent(title));
        }

        int maxLines = Math.min(lines.size(), 15);

        // Remove old entries if line count decreased
        for (int i = maxLines; i < currentLines.size(); i++) {
            String entry = entryNames.get(i);
            scoreboard.resetScores(entry);
        }

        // Update or set lines
        for (int i = 0; i < maxLines; i++) {
            String line = lines.get(i);
            Team team = lineTeams.get(i);
            String entry = entryNames.get(i);

            // Split line if longer than 64 characters (prefix max length safety)
            if (line.length() > 64) {
                String prefix = line.substring(0, 64);
                String suffix = line.substring(64, Math.min(line.length(), 128));
                team.prefix(TextUtil.toComponent(prefix));
                team.suffix(TextUtil.toComponent(suffix));
            } else {
                team.prefix(TextUtil.toComponent(line));
                team.suffix(Component.empty());
            }

            int score = maxLines - i;
            objective.getScore(entry).setScore(score);
        }

        currentLines.clear();
        for (int i = 0; i < maxLines; i++) {
            currentLines.add(lines.get(i));
        }
    }

    public void destroy() {
        if (player.isOnline()) {
            try {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (Exception ignored) {
            }
        }
    }

    public Player getPlayer() {
        return player;
    }
}
