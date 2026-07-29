package network.minespazio.spazioduels.pvp;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.duel.DuelMatch;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class PvP18Listener implements Listener {

    private final SpazioDuelsPlugin plugin;
    private final PvP18Manager pvpManager;

    public PvP18Listener(SpazioDuelsPlugin plugin, PvP18Manager pvpManager) {
        this.plugin = plugin;
        this.pvpManager = pvpManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker == null) return;

        // Check if victim is in an active duel
        DuelMatch match = plugin.getDuelManager().getMatch(victim);
        if (match == null || !match.isStarted() || match.isFinished()) return;

        if (!pvpManager.isPvP18Enabled()) return;

        // 1. Custom 1.8.9 Hit Detection (NoDamageTicks)
        int noDamageTicks = pvpManager.getNoDamageTicks();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (victim.isOnline() && !victim.isDead()) {
                victim.setNoDamageTicks(noDamageTicks);
            }
        });

        // 2. Custom 1.8.9 Knockback Physics
        if (pvpManager.isKnockbackEnabled()) {
            final Player finalAttacker = attacker;
            Vector dir = victim.getLocation().toVector().subtract(attacker.getLocation().toVector());
            if (dir.lengthSquared() == 0) {
                dir = attacker.getLocation().getDirection();
            }
            dir.setY(0).normalize();

            double horiz = pvpManager.getHorizontalKb();
            if (finalAttacker.isSprinting()) {
                horiz += pvpManager.getExtraSprintHorizontalKb();
            }

            double vert = Math.min(pvpManager.getVerticalLimit(), pvpManager.getVerticalKb());
            Vector customVelocity = dir.multiply(horiz).setY(vert);

            // Apply custom velocity on next tick to cleanly override vanilla 1.9+ KB calculation
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (victim.isOnline() && !victim.isDead()) {
                    victim.setVelocity(customVelocity);
                }
            });
        }
    }
}
