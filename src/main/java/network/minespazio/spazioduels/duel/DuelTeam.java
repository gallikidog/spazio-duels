package network.minespazio.spazioduels.duel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelTeam {

    private final String name;
    private final List<UUID> members = new ArrayList<>();
    private final Set<UUID> aliveMembers = new HashSet<>();
    private final Map<UUID, Double> damageDealt = new HashMap<>();
    private final Map<UUID, Integer> hitsDealt = new HashMap<>();

    public DuelTeam(String name, List<Player> players) {
        this.name = name;
        for (Player p : players) {
            this.members.add(p.getUniqueId());
            this.aliveMembers.add(p.getUniqueId());
            this.damageDealt.put(p.getUniqueId(), 0.0);
            this.hitsDealt.put(p.getUniqueId(), 0);
        }
    }

    public String getName() {
        return name;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getAliveMembers() {
        return aliveMembers;
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

    public List<Player> getOnlineAlivePlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : aliveMembers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                players.add(p);
            }
        }
        return players;
    }

    public void markDead(UUID uuid) {
        aliveMembers.remove(uuid);
    }

    public boolean isEliminated() {
        return aliveMembers.isEmpty();
    }

    public void addDamage(UUID uuid, double amount) {
        damageDealt.put(uuid, damageDealt.getOrDefault(uuid, 0.0) + amount);
    }

    public double getTotalDamage() {
        double total = 0.0;
        for (double d : damageDealt.values()) {
            total += d;
        }
        return total;
    }

    public void addHit(UUID uuid) {
        hitsDealt.put(uuid, hitsDealt.getOrDefault(uuid, 0) + 1);
    }

    public int getHits(UUID uuid) {
        return hitsDealt.getOrDefault(uuid, 0);
    }

    public String getFormattedMembers() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                names.add(p.getName());
            }
        }
        return String.join(", ", names);
    }
}
