package network.minespazio.spazioduels.duel;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HandicapRule {

    private boolean removeHelmet;
    private double healthModifier; // e.g. 0.8 for 80% health

    public HandicapRule() {
        this.removeHelmet = true;
        this.healthModifier = 1.0;
    }

    public HandicapRule(boolean removeHelmet, double healthModifier) {
        this.removeHelmet = removeHelmet;
        this.healthModifier = healthModifier;
    }

    public void applyHandicap(Player player) {
        if (player == null || !player.isOnline()) return;

        if (removeHelmet) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet != null) {
                player.getInventory().setHelmet(null);
            }
        }

        if (healthModifier > 0 && healthModifier < 1.0) {
            double newHealth = player.getMaxHealth() * healthModifier;
            player.setHealth(Math.min(player.getHealth(), newHealth));
        }
    }

    public boolean isRemoveHelmet() {
        return removeHelmet;
    }

    public double getHealthModifier() {
        return healthModifier;
    }
}
