/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.util.Vector
 */
package network.minespazio.spazioduels.pvp;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.pvp.PvP18Manager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class PvP18Listener
implements Listener {
    private final SpazioDuelsPlugin plugin;
    private final PvP18Manager pvpManager;

    public PvP18Listener(SpazioDuelsPlugin plugin, PvP18Manager pvpManager) {
        this.plugin = plugin;
        this.pvpManager = pvpManager;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
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
        if (attacker == null) {
            return;
        }
        DuelMatch match = this.plugin.getDuelManager().getMatch(victim);
        if (match == null || !match.isStarted() || match.isCountingDown() || match.isFinished()) {
            return;
        }
        if (!this.pvpManager.isPvP18Enabled()) {
            return;
        }
        int noDamageTicks = this.pvpManager.getNoDamageTicks();
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
            if (victim.isOnline() && !victim.isDead()) {
                victim.setNoDamageTicks(noDamageTicks);
            }
        });
        if (this.pvpManager.isKnockbackEnabled()) {
            Player finalAttacker = attacker;
            Vector dir = victim.getLocation().toVector().subtract(attacker.getLocation().toVector());
            if (dir.lengthSquared() == 0.0) {
                dir = attacker.getLocation().getDirection();
            }
            dir.setY(0).normalize();
            double horiz = this.pvpManager.getHorizontalKb();
            if (finalAttacker.isSprinting()) {
                horiz += this.pvpManager.getExtraSprintHorizontalKb();
            }
            double vert = Math.min(this.pvpManager.getVerticalLimit(), this.pvpManager.getVerticalKb());
            Vector customVelocity = dir.multiply(horiz).setY(vert);
            Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> {
                if (victim.isOnline() && !victim.isDead()) {
                    victim.setVelocity(customVelocity);
                }
            });
        }
    }
}

