package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MatchListener implements Listener {

    private final SpazioDuelsPlugin plugin;

    public MatchListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        DuelMatch match = plugin.getDuelManager().getMatch(victim);

        if (match != null) {
            event.getDrops().clear(); // Anti-dupe: clear drops on death in duel
            event.setDroppedExp(0);

            Player killer = victim.getKiller();
            match.handlePlayerDeath(victim, killer);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = plugin.getDuelManager().getMatch(player);

        if (match != null) {
            match.handlePlayerQuit(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null) {
            DuelMatch matchVictim = plugin.getDuelManager().getMatch(victim);
            DuelMatch matchAttacker = plugin.getDuelManager().getMatch(attacker);

            if (matchVictim != null && matchAttacker != null && matchVictim.equals(matchAttacker)) {
                DuelTeam teamVictim = matchVictim.getTeamOf(victim);
                DuelTeam teamAttacker = matchAttacker.getTeamOf(attacker);

                // Cancel Friendly Fire
                if (teamVictim != null && teamVictim.equals(teamAttacker)) {
                    event.setCancelled(true);
                    return;
                }

                // Track damage dealt
                if (teamAttacker != null) {
                    teamAttacker.addDamage(attacker.getUniqueId(), event.getFinalDamage());
                }
            } else if (matchVictim != null || matchAttacker != null) {
                // Prevent interference from outside players
                event.setCancelled(true);
            }
        }
    }
}
