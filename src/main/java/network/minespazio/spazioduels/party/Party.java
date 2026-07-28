package network.minespazio.spazioduels.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Party {

    private final UUID id;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> pendingInvites = new HashSet<>();

    public Party(Player leader) {
        this.id = UUID.randomUUID();
        this.leader = leader.getUniqueId();
        this.members.add(leader.getUniqueId());
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                players.add(p);
            }
        }
        return players;
    }

    public boolean addMember(Player player) {
        pendingInvites.remove(player.getUniqueId());
        return members.add(player.getUniqueId());
    }

    public boolean removeMember(UUID uuid) {
        boolean removed = members.remove(uuid);
        if (removed && leader.equals(uuid) && !members.isEmpty()) {
            leader = members.iterator().next(); // Transfer leadership
        }
        return removed;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public void invite(Player player) {
        pendingInvites.add(player.getUniqueId());
    }

    public boolean hasInvite(UUID uuid) {
        return pendingInvites.contains(uuid);
    }

    public String getFormattedMembers() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : members) {
            org.bukkit.OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            if (p.getName() != null) {
                names.add(p.getName());
            }
        }
        return String.join(", ", names);
    }

    public void broadcast(String message) {
        for (Player p : getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }

    public int getSize() {
        return members.size();
    }
}
