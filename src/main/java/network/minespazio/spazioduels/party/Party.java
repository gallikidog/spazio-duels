/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Party {
    private final UUID id;
    private UUID leader;
    private final Set<UUID> members = new HashSet<UUID>();
    private final Set<UUID> pendingInvites = new HashSet<UUID>();

    public Party(Player leader) {
        this.id = UUID.randomUUID();
        this.leader = leader.getUniqueId();
        this.members.add(leader.getUniqueId());
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getLeader() {
        return this.leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return this.members;
    }

    public List<Player> getOnlinePlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        for (UUID uuid : this.members) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null || !p.isOnline()) continue;
            players.add(p);
        }
        return players;
    }

    public boolean addMember(Player player) {
        this.pendingInvites.remove(player.getUniqueId());
        return this.members.add(player.getUniqueId());
    }

    public boolean removeMember(UUID uuid) {
        boolean removed = this.members.remove(uuid);
        if (removed && this.leader.equals(uuid) && !this.members.isEmpty()) {
            this.leader = this.members.iterator().next();
        }
        return removed;
    }

    public boolean isMember(UUID uuid) {
        return this.members.contains(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return this.leader.equals(uuid);
    }

    public void invite(Player player) {
        this.pendingInvites.add(player.getUniqueId());
    }

    public boolean hasInvite(UUID uuid) {
        return this.pendingInvites.contains(uuid);
    }

    public String getFormattedMembers() {
        ArrayList<String> names = new ArrayList<String>();
        for (UUID uuid : this.members) {
            OfflinePlayer p = Bukkit.getOfflinePlayer((UUID)uuid);
            if (p.getName() == null) continue;
            names.add(p.getName());
        }
        return String.join((CharSequence)", ", names);
    }

    public void broadcast(String message) {
        for (Player p : this.getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }

    public int getSize() {
        return this.members.size();
    }
}

