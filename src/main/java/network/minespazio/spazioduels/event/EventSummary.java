package network.minespazio.spazioduels.event;

import network.minespazio.spazioduels.util.RewardUtil;

import java.util.List;
import java.util.UUID;

public class EventSummary {

    private final UUID eventId;
    private final String eventName;
    private final String modeName;
    private final String kitName;
    private final String winnerName;
    private final String winnerTeamName;
    private final List<RewardUtil.RewardItem> rewards;
    private final long durationSeconds;
    private final int totalMatches;
    private final int totalKills;

    public EventSummary(UUID eventId, String eventName, String modeName, String kitName, String winnerName, String winnerTeamName, List<RewardUtil.RewardItem> rewards, long durationSeconds, int totalMatches, int totalKills) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.modeName = modeName;
        this.kitName = kitName;
        this.winnerName = winnerName;
        this.winnerTeamName = winnerTeamName;
        this.rewards = rewards;
        this.durationSeconds = durationSeconds;
        this.totalMatches = totalMatches;
        this.totalKills = totalKills;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getModeName() {
        return modeName;
    }

    public String getKitName() {
        return kitName;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public String getWinnerTeamName() {
        return winnerTeamName;
    }

    public List<RewardUtil.RewardItem> getRewards() {
        return rewards;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public int getTotalKills() {
        return totalKills;
    }
}
