/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package network.minespazio.spazioduels.event;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.event.DuelEvent;
import network.minespazio.spazioduels.event.EventState;
import network.minespazio.spazioduels.event.EventSummary;
import network.minespazio.spazioduels.gui.EventSummaryGUI;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.koth.KothSchedule;
import network.minespazio.spazioduels.util.RewardUtil;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelEventManager {
    private final SpazioDuelsPlugin plugin;
    private DuelEvent activeEvent;
    private final Map<UUID, EventSummary> summaries = new HashMap<UUID, EventSummary>();
    private final Map<UUID, List<RewardUtil.RewardItem>> pendingRewards = new HashMap<UUID, List<RewardUtil.RewardItem>>();
    private LocalDateTime nextAutoStartAt;
    private String lastScheduledActivationMinute;

    public DuelEventManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.startAutoSchedule();
    }

    public DuelEvent startEvent(String eventName, DuelMode mode, Kit kit) {
        return this.startEvent(eventName, mode, kit, false);
    }

    private DuelEvent startEvent(String eventName, DuelMode mode, Kit kit, boolean automatic) {
        if (this.activeEvent != null && this.activeEvent.getState() != EventState.FINISHED) {
            return this.activeEvent;
        }
        List<RewardUtil.RewardItem> rewardsPool = RewardUtil.loadRewardsFromConfig(this.plugin.getConfig().getConfigurationSection("rewards"));
        this.activeEvent = new DuelEvent(this.plugin, eventName, mode, kit, rewardsPool, automatic);
        this.activeEvent.announceStart();
        if (automatic) {
            final DuelEvent scheduledEvent = this.activeEvent;
            int timeoutSeconds = Math.max(1, this.plugin.getConfig().getInt("event.automatic_max_duration_seconds", 300));
            new BukkitRunnable(){

                public void run() {
                    if (DuelEventManager.this.activeEvent == scheduledEvent && scheduledEvent.getState() != EventState.FINISHED) {
                        scheduledEvent.cancelDueToTimeout();
                    }
                }
            }.runTaskLater((Plugin)this.plugin, (long)timeoutSeconds * 20L);
        }
        final int lobbyTime = this.plugin.getConfig().getInt("event.lobby_countdown", 45);
        new BukkitRunnable(){
            int secondsRemaining;
            {
                this.secondsRemaining = lobbyTime;
            }

            public void run() {
                if (DuelEventManager.this.activeEvent == null || DuelEventManager.this.activeEvent.getState() == EventState.FINISHED) {
                    this.cancel();
                    return;
                }
                if (this.secondsRemaining <= 0) {
                    this.cancel();
                    DuelEventManager.this.activeEvent.startTournament();
                } else {
                    if (this.secondsRemaining == 30 || this.secondsRemaining == 15 || this.secondsRemaining == 5) {
                        DuelEventManager.this.activeEvent.announceStart();
                    }
                    this.secondsRemaining -= 5;
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 100L, 100L);
        return this.activeEvent;
    }

    private void startAutoSchedule() {
        if (!this.plugin.getConfig().getBoolean("duel_event_autostart.enabled", false)) {
            this.nextAutoStartAt = null;
            return;
        }
        final List activationTimes = this.plugin.getConfig().getStringList("duel_event_autostart.activation_times");
        if (activationTimes != null && !activationTimes.isEmpty()) {
            this.updateNextScheduledActivation(activationTimes);
            new BukkitRunnable(){

                public void run() {
                    LocalDateTime now = LocalDateTime.now();
                    DuelEventManager.this.updateNextScheduledActivation(activationTimes);
                    String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());
                    if (!DuelEventManager.this.isScheduledActivation(activationTimes, currentTime)) {
                        return;
                    }
                    String minuteKey = String.format("%04d-%02d-%02dT%s", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), currentTime);
                    if (minuteKey.equals(DuelEventManager.this.lastScheduledActivationMinute)) {
                        return;
                    }
                    DuelEventManager.this.lastScheduledActivationMinute = minuteKey;
                    DuelEventManager.this.startAutomaticEvent();
                }
            }.runTaskTimer((Plugin)this.plugin, 20L, 20L);
            return;
        }
        int intervalMinutes = this.plugin.getConfig().getInt("duel_event_autostart.interval_minutes", 90);
        if (intervalMinutes < 1) {
            intervalMinutes = 1;
        }
        final int safeIntervalMinutes = intervalMinutes;
        this.nextAutoStartAt = LocalDateTime.now().plusMinutes(safeIntervalMinutes);
        long ticks = (long)(intervalMinutes * 60) * 20L;
        new BukkitRunnable(){

            public void run() {
                DuelEventManager.this.nextAutoStartAt = LocalDateTime.now().plusMinutes(safeIntervalMinutes);
                DuelEventManager.this.startAutomaticEvent();
            }
        }.runTaskTimer((Plugin)this.plugin, ticks, ticks);
    }

    private void startAutomaticEvent() {
        List<Kit> enabledKits;
        DuelMode selectedMode;
        if (this.activeEvent != null && this.activeEvent.getState() != EventState.FINISHED) {
            return;
        }
        List configuredModes = this.plugin.getConfig().getStringList("duel_event_autostart.modes_pool");
        if (configuredModes != null && !configuredModes.isEmpty()) {
            String rawMode = (String)configuredModes.get(new Random().nextInt(configuredModes.size()));
            selectedMode = DuelMode.fromString(rawMode);
        } else {
            DuelMode[] modes = DuelMode.values();
            selectedMode = modes[new Random().nextInt(modes.length)];
        }
        List configuredKits = this.plugin.getConfig().getStringList("duel_event_autostart.kits_pool");
        Kit selectedKit = null;
        if (configuredKits != null && !configuredKits.isEmpty()) {
            String kitName = (String)configuredKits.get(new Random().nextInt(configuredKits.size()));
            selectedKit = this.plugin.getKitManager().getKit(kitName);
        }
        if (selectedKit == null && !(enabledKits = this.plugin.getKitManager().getEnabledKitsForDuels()).isEmpty()) {
            selectedKit = enabledKits.get(new Random().nextInt(enabledKits.size()));
        }
        if (selectedKit != null) {
            this.startEvent("Torneo de Duelos Autom\u00e1tico", selectedMode, selectedKit, true);
            this.plugin.getLogger().info("Torneo autom\u00e1tico de duelos iniciado (Modo: " + selectedMode.getDisplayName() + ", Kit: " + selectedKit.getName() + ").");
        }
    }

    private boolean isScheduledActivation(List<String> activationTimes, String currentTime) {
        for (String activationTime : activationTimes) {
            if (!currentTime.equals(KothSchedule.normalizeTime(activationTime))) continue;
            return true;
        }
        return false;
    }

    private void updateNextScheduledActivation(List<String> activationTimes) {
        this.nextAutoStartAt = KothSchedule.getNextActivation(activationTimes, LocalDateTime.now());
    }

    public LocalDateTime getNextAutoStartAt() {
        return this.nextAutoStartAt;
    }

    public String getNextAutoStartCountdown() {
        if (this.nextAutoStartAt == null) {
            return "--:--:--";
        }
        long seconds = Math.max(0L, Duration.between(LocalDateTime.now(), this.nextAutoStartAt).getSeconds());
        return String.format("%02d:%02d:%02d", seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
    }

    public void registerSummary(EventSummary summary) {
        if (summary != null) {
            this.summaries.put(summary.getEventId(), summary);
        }
    }

    public EventSummary getSummary(UUID eventId) {
        return this.summaries.get(eventId);
    }

    public EventSummary getLatestSummary() {
        if (this.summaries.isEmpty()) {
            return null;
        }
        ArrayList<EventSummary> list = new ArrayList<EventSummary>(this.summaries.values());
        return list.get(list.size() - 1);
    }

    public void queueRewardsForDelivery(Player player, List<RewardUtil.RewardItem> rewards) {
        if (player == null || rewards == null || rewards.isEmpty()) {
            return;
        }
        this.pendingRewards.put(player.getUniqueId(), new ArrayList<RewardUtil.RewardItem>(rewards));
    }

    public void deliverPendingRewards(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        List<RewardUtil.RewardItem> rewards = this.pendingRewards.remove(player.getUniqueId());
        if (rewards == null || rewards.isEmpty()) {
            return;
        }
        RewardUtil.executeRewards(player, rewards);
        player.sendMessage(TextUtil.colorize("&aTus recompensas del evento de duelos han sido entregadas."));
    }

    public void openSummaryGUI(Player player, UUID eventId) {
        EventSummary summary = eventId != null ? this.getSummary(eventId) : this.getLatestSummary();
        EventSummary eventSummary = summary;
        if (summary == null) {
            player.sendMessage(TextUtil.colorize("&cNo se encontr\u00f3 informaci\u00f3n para este evento."));
            return;
        }
        EventSummaryGUI gui = new EventSummaryGUI(this.plugin, summary);
        gui.open(player);
    }

    public DuelEvent getActiveEvent() {
        return this.activeEvent;
    }
}

