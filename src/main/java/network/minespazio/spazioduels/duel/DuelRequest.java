/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.duel;

import java.util.List;
import java.util.UUID;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.kit.Kit;
import org.bukkit.entity.Player;

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
        return this.sender;
    }

    public UUID getTarget() {
        return this.target;
    }

    public List<Player> getTeam1() {
        return this.team1;
    }

    public List<Player> getTeam2() {
        return this.team2;
    }

    public Kit getKit() {
        return this.kit;
    }

    public DuelMode getMode() {
        return this.mode;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.createdAt > 60000L;
    }
}

