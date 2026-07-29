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
        return plugin.getConfig().getBoolean("pvp_1_8.enabled", true);
    }

    public boolean isDisableAttackCooldownEnabled() {
        return isPvP18Enabled() && plugin.getConfig().getBoolean("pvp_1_8.disable_attack_cooldown", true);
    }

    public int getNoDamageTicks() {
        return plugin.getConfig().getInt("pvp_1_8.no_damage_ticks", 10);
    }

    public boolean isKnockbackEnabled() {
        return isPvP18Enabled() && plugin.getConfig().getBoolean("pvp_1_8.knockback.enabled", true);
    }

    public double getHorizontalKb() {
        return plugin.getConfig().getDouble("pvp_1_8.knockback.horizontal", 0.4);
    }

    public double getVerticalKb() {
        return plugin.getConfig().getDouble("pvp_1_8.knockback.vertical", 0.36);
    }

    public double getExtraSprintHorizontalKb() {
        return plugin.getConfig().getDouble("pvp_1_8.knockback.extra_sprint_horizontal", 0.15);
    }

    public double getVerticalLimit() {
        return plugin.getConfig().getDouble("pvp_1_8.knockback.vertical_limit", 0.45);
    }

    public void enable18PvP(Player player) {
        if (!isDisableAttackCooldownEnabled()) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(16.0); // 16.0 removes attack cooldown indicator and cooldown delay
        }
    }

    public void restoreVanillaPvP(Player player) {
        if (player == null || !player.isOnline()) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(4.0); // Restore vanilla 1.9+ attack speed default
        }
    }
}
