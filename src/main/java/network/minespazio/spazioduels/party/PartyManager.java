/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.party;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.party.Party;
import org.bukkit.entity.Player;

public class PartyManager {
    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, Party> partyByPlayer = new HashMap<UUID, Party>();
    private final Map<UUID, Party> partiesById = new HashMap<UUID, Party>();

    public PartyManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public Party createParty(Player leader) {
        if (this.hasParty(leader)) {
            return this.getParty(leader);
        }
        Party party = new Party(leader);
        this.partiesById.put(party.getId(), party);
        this.partyByPlayer.put(leader.getUniqueId(), party);
        return party;
    }

    public Party getParty(Player player) {
        if (player == null) {
            return null;
        }
        return this.partyByPlayer.get(player.getUniqueId());
    }

    public boolean hasParty(Player player) {
        if (player == null) {
            return false;
        }
        return this.partyByPlayer.containsKey(player.getUniqueId());
    }

    public void joinParty(Player player, Party party) {
        if (player == null || party == null) {
            return;
        }
        party.addMember(player);
        this.partyByPlayer.put(player.getUniqueId(), party);
    }

    public void leaveParty(Player player) {
        if (player == null) {
            return;
        }
        Party party = this.partyByPlayer.remove(player.getUniqueId());
        if (party != null) {
            party.removeMember(player.getUniqueId());
            if (party.getMembers().isEmpty()) {
                this.partiesById.remove(party.getId());
            } else {
                party.broadcast("&c" + player.getName() + " ha salido de la party.");
            }
        }
    }

    public void disbandParty(Party party) {
        if (party == null) {
            return;
        }
        party.broadcast("&cLa party ha sido disuelta por el l\u00edder.");
        for (UUID uuid : new HashSet<UUID>(party.getMembers())) {
            this.partyByPlayer.remove(uuid);
        }
        this.partiesById.remove(party.getId());
    }
}

