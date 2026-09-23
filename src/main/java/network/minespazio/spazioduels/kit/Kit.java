/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 */
package network.minespazio.spazioduels.kit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class Kit {
    private final String name;
    private ItemStack icon;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private ItemStack offHand;
    private List<PotionEffect> potionEffects;
    private boolean allowBuilding;
    private boolean enabledForDuels;
    private boolean fromPlayerKits;

    public Kit(String name) {
        this.name = name;
        this.icon = new ItemStack(Material.DIAMOND_SWORD);
        this.contents = new ItemStack[36];
        this.armor = new ItemStack[4];
        this.offHand = null;
        this.potionEffects = new ArrayList<PotionEffect>();
        this.allowBuilding = false;
        this.enabledForDuels = true;
        this.fromPlayerKits = false;
    }

    public Kit(String name, ItemStack icon, ItemStack[] contents, ItemStack[] armor, ItemStack offHand, Collection<PotionEffect> potionEffects, boolean allowBuilding, boolean enabledForDuels, boolean fromPlayerKits) {
        this.name = name;
        this.icon = icon != null ? icon.clone() : new ItemStack(Material.DIAMOND_SWORD);
        this.contents = new ItemStack[36];
        if (contents != null) {
            System.arraycopy(contents, 0, this.contents, 0, Math.min(contents.length, 36));
        }
        this.armor = new ItemStack[4];
        if (armor != null) {
            System.arraycopy(armor, 0, this.armor, 0, Math.min(armor.length, 4));
        }
        this.offHand = offHand != null ? offHand.clone() : null;
        this.potionEffects = potionEffects != null ? new ArrayList<PotionEffect>(potionEffects) : new ArrayList();
        this.allowBuilding = allowBuilding;
        this.enabledForDuels = enabledForDuels;
        this.fromPlayerKits = fromPlayerKits;
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack[] getContents() {
        return this.contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmor() {
        return this.armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public ItemStack getOffHand() {
        return this.offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public List<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public boolean isAllowBuilding() {
        return this.allowBuilding;
    }

    public void setAllowBuilding(boolean allowBuilding) {
        this.allowBuilding = allowBuilding;
    }

    public boolean isEnabledForDuels() {
        return this.enabledForDuels;
    }

    public void setEnabledForDuels(boolean enabledForDuels) {
        this.enabledForDuels = enabledForDuels;
    }

    public boolean isFromPlayerKits() {
        return this.fromPlayerKits;
    }

    public void setFromPlayerKits(boolean fromPlayerKits) {
        this.fromPlayerKits = fromPlayerKits;
    }
}

