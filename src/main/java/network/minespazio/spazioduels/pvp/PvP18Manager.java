/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.attribute.Attribute
 *  org.bukkit.attribute.AttributeInstance
 *  org.bukkit.entity.Player
 */
package network.minespazio.spazioduels.pvp;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class PvP18Manager {
    private final SpazioDuelsPlugin plugin;

    public PvP18Manager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPvP18Enabled() {
        return this.plugin.getConfig().getBoolean("pvp_1_8.enabled", true);
    }

    public boolean isDisableAttackCooldownEnabled() {
        return this.isPvP18Enabled() && this.plugin.getConfig().getBoolean("pvp_1_8.disable_attack_cooldown", true);
    }

    public int getNoDamageTicks() {
        return this.plugin.getConfig().getInt("pvp_1_8.no_damage_ticks", 10);
    }

    public boolean isKnockbackEnabled() {
        return this.isPvP18Enabled() && this.plugin.getConfig().getBoolean("pvp_1_8.knockback.enabled", true);
    }

    public double getHorizontalKb() {
        return this.plugin.getConfig().getDouble("pvp_1_8.knockback.horizontal", 0.4);
    }

    public double getVerticalKb() {
        return this.plugin.getConfig().getDouble("pvp_1_8.knockback.vertical", 0.36);
    }

    public double getExtraSprintHorizontalKb() {
        return this.plugin.getConfig().getDouble("pvp_1_8.knockback.extra_sprint_horizontal", 0.15);
    }

    public double getVerticalLimit() {
        return this.plugin.getConfig().getDouble("pvp_1_8.knockback.vertical_limit", 0.45);
    }

    public void enable18PvP(Player player) {
        if (!this.isDisableAttackCooldownEnabled()) {
            return;
        }
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(16.0);
        }
    }

    public void restoreVanillaPvP(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(4.0);
        }
    }
}

