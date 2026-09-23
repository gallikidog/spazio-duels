/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DuelTeam {
    private final String name;
    private final List<UUID> members = new ArrayList<UUID>();
    private final Set<UUID> aliveMembers = new HashSet<UUID>();
    private final Map<UUID, Double> damageDealt = new HashMap<UUID, Double>();
    private final Map<UUID, Integer> hitsDealt = new HashMap<UUID, Integer>();

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
        return this.name;
    }

    public List<UUID> getMembers() {
        return this.members;
    }

    public Set<UUID> getAliveMembers() {
        return this.aliveMembers;
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

    public List<Player> getOnlineAlivePlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        for (UUID uuid : this.aliveMembers) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null || !p.isOnline()) continue;
            players.add(p);
        }
        return players;
    }

    public void markDead(UUID uuid) {
        this.aliveMembers.remove(uuid);
    }

    public boolean isEliminated() {
        return this.aliveMembers.isEmpty();
    }

    public void addDamage(UUID uuid, double amount) {
        this.damageDealt.put(uuid, this.damageDealt.getOrDefault(uuid, 0.0) + amount);
    }

    public double getTotalDamage() {
        double total = 0.0;
        for (double d : this.damageDealt.values()) {
            total += d;
        }
        return total;
    }

    public void addHit(UUID uuid) {
        this.hitsDealt.put(uuid, this.hitsDealt.getOrDefault(uuid, 0) + 1);
    }

    public int getHits(UUID uuid) {
        return this.hitsDealt.getOrDefault(uuid, 0);
    }

    public String getFormattedMembers() {
        ArrayList<String> names = new ArrayList<String>();
        for (UUID uuid : this.members) {
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null) continue;
            names.add(p.getName());
        }
        return String.join((CharSequence)", ", names);
    }
}

