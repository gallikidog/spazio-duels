package network.minespazio.spazioduels.event;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.gui.EventSummaryGUI;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.RewardUtil;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelEventManager {

    private final SpazioDuelsPlugin plugin;
    private DuelEvent activeEvent;
    private final Map<UUID, EventSummary> summaries = new HashMap<>();

    public DuelEventManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public DuelEvent startEvent(String eventName, DuelMode mode, Kit kit) {
        if (activeEvent != null && activeEvent.getState() != EventState.FINISHED) {
            return activeEvent;
        }

        List<RewardUtil.RewardItem> rewardsPool = RewardUtil.loadRewardsFromConfig(plugin.getConfig().getConfigurationSection("rewards"));
        activeEvent = new DuelEvent(plugin, eventName, mode, kit, rewardsPool);
        activeEvent.announceStart();

        int lobbyTime = plugin.getConfig().getInt("event.lobby_countdown", 45);

        // Lobby timer countdown task
        new BukkitRunnable() {
            int secondsRemaining = lobbyTime;

            @Override
            public void run() {
                if (activeEvent == null || activeEvent.getState() == EventState.FINISHED) {
                    cancel();
                    return;
                }

                if (secondsRemaining <= 0) {
                    cancel();
                    activeEvent.startTournament();
                } else {
                    if (secondsRemaining == 30 || secondsRemaining == 15 || secondsRemaining == 5) {
                        activeEvent.announceStart();
                    }
                    secondsRemaining -= 5;
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // Every 5s

        return activeEvent;
    }

    public void registerSummary(EventSummary summary) {
        if (summary != null) {
            summaries.put(summary.getEventId(), summary);
        }
    }

    public EventSummary getSummary(UUID eventId) {
        return summaries.get(eventId);
    }

    public EventSummary getLatestSummary() {
        if (summaries.isEmpty()) return null;
        List<EventSummary> list = new ArrayList<>(summaries.values());
        return list.get(list.size() - 1);
    }

    public void openSummaryGUI(Player player, UUID eventId) {
        EventSummary summary = (eventId != null) ? getSummary(eventId) : getLatestSummary();
        if (summary == null) {
            player.sendMessage(TextUtil.colorize("&cNo se encontró información para este evento."));
            return;
        }

        EventSummaryGUI gui = new EventSummaryGUI(plugin, summary);
        gui.open(player);
    }

    public DuelEvent getActiveEvent() {
        return activeEvent;
    }
}
