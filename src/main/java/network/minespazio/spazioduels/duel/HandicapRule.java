/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.duel;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HandicapRule {
    private boolean removeHelmet;
    private double healthModifier;

    public HandicapRule() {
        this.removeHelmet = true;
        this.healthModifier = 1.0;
    }

    public HandicapRule(boolean removeHelmet, double healthModifier) {
        this.removeHelmet = removeHelmet;
        this.healthModifier = healthModifier;
    }

    public void applyHandicap(Player player) {
        ItemStack helmet;
        if (player == null || !player.isOnline()) {
            return;
        }
        if (this.removeHelmet && (helmet = player.getInventory().getHelmet()) != null) {
            player.getInventory().setHelmet(null);
        }
        if (this.healthModifier > 0.0 && this.healthModifier < 1.0) {
            double newHealth = player.getMaxHealth() * this.healthModifier;
            player.setHealth(Math.min(player.getHealth(), newHealth));
        }
    }

    public boolean isRemoveHelmet() {
        return this.removeHelmet;
    }

    public double getHealthModifier() {
        return this.healthModifier;
    }
}

