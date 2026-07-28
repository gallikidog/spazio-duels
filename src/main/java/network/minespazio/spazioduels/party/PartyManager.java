package network.minespazio.spazioduels.party;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<UUID, Party> partyByPlayer = new HashMap<>();
    private final Map<UUID, Party> partiesById = new HashMap<>();

    public PartyManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public Party createParty(Player leader) {
        if (hasParty(leader)) {
            return getParty(leader);
        }
        Party party = new Party(leader);
        partiesById.put(party.getId(), party);
        partyByPlayer.put(leader.getUniqueId(), party);
        return party;
    }

    public Party getParty(Player player) {
        if (player == null) return null;
        return partyByPlayer.get(player.getUniqueId());
    }

    public boolean hasParty(Player player) {
        if (player == null) return false;
        return partyByPlayer.containsKey(player.getUniqueId());
    }

    public void joinParty(Player player, Party party) {
        if (player == null || party == null) return;
        party.addMember(player);
        partyByPlayer.put(player.getUniqueId(), party);
    }

    public void leaveParty(Player player) {
        if (player == null) return;
        Party party = partyByPlayer.remove(player.getUniqueId());
        if (party != null) {
            party.removeMember(player.getUniqueId());
            if (party.getMembers().isEmpty()) {
                partiesById.remove(party.getId());
            } else {
                party.broadcast("&c" + player.getName() + " ha salido de la party.");
            }
        }
    }

    public void disbandParty(Party party) {
        if (party == null) return;
        party.broadcast("&cLa party ha sido disuelta por el líder.");
        for (UUID uuid : new HashSet<>(party.getMembers())) {
            partyByPlayer.remove(uuid);
        }
        partiesById.remove(party.getId());
    }
}
