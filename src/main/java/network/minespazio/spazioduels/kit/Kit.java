package network.minespazio.spazioduels.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        this.potionEffects = new ArrayList<>();
        this.allowBuilding = false;
        this.enabledForDuels = true;
        this.fromPlayerKits = false;
    }

    public Kit(String name, ItemStack icon, ItemStack[] contents, ItemStack[] armor, ItemStack offHand, Collection<PotionEffect> potionEffects, boolean allowBuilding, boolean enabledForDuels, boolean fromPlayerKits) {
        this.name = name;
        this.icon = icon != null ? icon.clone() : new ItemStack(Material.DIAMOND_SWORD);
        this.contents = contents != null ? contents : new ItemStack[36];
        this.armor = armor != null ? armor : new ItemStack[4];
        this.offHand = offHand;
        this.potionEffects = potionEffects != null ? new ArrayList<>(potionEffects) : new ArrayList<>();
        this.allowBuilding = allowBuilding;
        this.enabledForDuels = enabledForDuels;
        this.fromPlayerKits = fromPlayerKits;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public void setArmor(ItemStack[] armor) {
        this.armor = armor;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public boolean isAllowBuilding() {
        return allowBuilding;
    }

    public void setAllowBuilding(boolean allowBuilding) {
        this.allowBuilding = allowBuilding;
    }

    public boolean isEnabledForDuels() {
        return enabledForDuels;
    }

    public void setEnabledForDuels(boolean enabledForDuels) {
        this.enabledForDuels = enabledForDuels;
    }

    public boolean isFromPlayerKits() {
        return fromPlayerKits;
    }

    public void setFromPlayerKits(boolean fromPlayerKits) {
        this.fromPlayerKits = fromPlayerKits;
    }
}
