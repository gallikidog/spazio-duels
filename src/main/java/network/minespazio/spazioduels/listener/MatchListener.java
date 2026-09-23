/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package network.minespazio.spazioduels.listener;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.duel.DuelTeam;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MatchListener
implements Listener {
    private final SpazioDuelsPlugin plugin;

    public MatchListener(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        DuelMatch match = this.plugin.getDuelManager().getMatch(victim);
        if (match != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            Player killer = victim.getKiller();
            match.handlePlayerDeath(victim, killer);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DuelMatch match = this.plugin.getDuelManager().getMatch(player);
        if (match != null) {
            match.handlePlayerQuit(player);
        }
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player victim = (Player)entity;
        Player attacker = null;
        Entity entity2 = event.getDamager();
        if (entity2 instanceof Player) {
            Player p;
            attacker = p = (Player)entity2;
        } else {
            Projectile proj;
            entity2 = event.getDamager();
            if (entity2 instanceof Projectile && (proj = (Projectile)entity2).getShooter() instanceof Player) {
                Player p;
                attacker = p = (Player)proj.getShooter();
            }
        }
        if (attacker != null) {
            DuelMatch matchVictim = this.plugin.getDuelManager().getMatch(victim);
            DuelMatch matchAttacker = this.plugin.getDuelManager().getMatch(attacker);
            if (matchVictim != null && matchAttacker != null && matchVictim.equals(matchAttacker)) {
                if (!matchVictim.isStarted() || matchVictim.isCountingDown() || matchVictim.isFinished()) {
                    event.setCancelled(true);
                    return;
                }
                DuelTeam teamVictim = matchVictim.getTeamOf(victim);
                DuelTeam teamAttacker = matchAttacker.getTeamOf(attacker);
                if (teamVictim != null && teamVictim.equals(teamAttacker)) {
                    event.setCancelled(true);
                    return;
                }
                if (teamAttacker != null) {
                    teamAttacker.addDamage(attacker.getUniqueId(), event.getFinalDamage());
                    teamAttacker.addHit(attacker.getUniqueId());
                }
            } else if (matchVictim != null || matchAttacker != null) {
                event.setCancelled(true);
            }
        }
    }
}

