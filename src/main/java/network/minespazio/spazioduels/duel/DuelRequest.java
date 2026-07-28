package network.minespazio.spazioduels.duel;

import network.minespazio.spazioduels.kit.Kit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DuelRequest {

    private final UUID sender;
    private final UUID target;
    private final List<Player> team1;
    private final List<Player> team2;
    private final Kit kit;
    private final DuelMode mode;
    private final long createdAt;

    public DuelRequest(Player sender, Player target, List<Player> team1, List<Player> team2, Kit kit, DuelMode mode) {
        this.sender = sender.getUniqueId();
        this.target = target.getUniqueId();
        this.team1 = team1;
        this.team2 = team2;
        this.kit = kit;
        this.mode = mode;
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getTarget() {
        return target;
    }

    public List<Player> getTeam1() {
        return team1;
    }

    public List<Player> getTeam2() {
        return team2;
    }

    public Kit getKit() {
        return kit;
    }

    public DuelMode getMode() {
        return mode;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 60000; // 60s expiration
    }
}
