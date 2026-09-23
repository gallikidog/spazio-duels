/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.potion.PotionEffect
 *  pk.ajneb97.PlayerKits2
 *  pk.ajneb97.api.PlayerKitsAPI
 *  pk.ajneb97.model.Kit
 *  pk.ajneb97.model.internal.GiveKitInstructions
 *  pk.ajneb97.model.internal.PlayerKitsMessageResult
 *  pk.ajneb97.model.item.KitItem
 */
package network.minespazio.spazioduels.integration;

import java.util.ArrayList;
import java.util.List;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.model.Kit;
import pk.ajneb97.model.internal.GiveKitInstructions;
import pk.ajneb97.model.internal.PlayerKitsMessageResult;
import pk.ajneb97.model.item.KitItem;

public class PlayerKitsHook {
    private final SpazioDuelsPlugin plugin;
    private boolean enabled;

    public PlayerKitsHook(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("PlayerKits2");
        if (this.enabled) {
            plugin.getLogger().info("PlayerKits2 detectado. Se habilitar\u00e1 la integraci\u00f3n autom\u00e1tica de kits.");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public List<network.minespazio.spazioduels.kit.Kit> importKitsFromPlayerKits() {
        ArrayList<network.minespazio.spazioduels.kit.Kit> importedKits = new ArrayList<network.minespazio.spazioduels.kit.Kit>();
        if (!this.enabled) {
            return importedKits;
        }
        try {
            PlayerKits2 pkPlugin = PlayerKitsAPI.getPlugin();
            if (pkPlugin == null || pkPlugin.getKitsManager() == null) {
                return importedKits;
            }
            ArrayList pkKits = pkPlugin.getKitsManager().getKits();
            if (pkKits == null) {
                return importedKits;
            }
            for (Object rawKit : pkKits) {
                if (!(rawKit instanceof Kit)) continue;
                Kit pkKit = (Kit)rawKit;
                String kitName = pkKit.getName();
                ItemStack icon = null;
                if (pkKit.getDisplayItemDefault() != null) {
                    icon = pkPlugin.getKitItemManager().createItemFromKitItem(pkKit.getDisplayItemDefault(), null, pkKit);
                }
                if (icon == null || icon.getType() == Material.AIR) {
                    icon = new ItemStack(Material.DIAMOND_SWORD);
                }
                ItemStack[] contents = new ItemStack[36];
                ItemStack[] armor = new ItemStack[4];
                ItemStack offHand = null;
                if (pkKit.getItems() != null) {
                    block5: for (KitItem kitItem : pkKit.getItems()) {
                        ItemStack item = pkPlugin.getKitItemManager().createItemFromKitItem(kitItem, null, pkKit);
                        if (item == null || item.getType() == Material.AIR) continue;
                        Material type = item.getType();
                        String typeName = type.name();
                        if (kitItem.isOffhand()) {
                            offHand = item;
                            continue;
                        }
                        if (typeName.endsWith("_HELMET") && armor[3] == null) {
                            armor[3] = item;
                            continue;
                        }
                        if (typeName.endsWith("_CHESTPLATE") && armor[2] == null) {
                            armor[2] = item;
                            continue;
                        }
                        if (typeName.endsWith("_LEGGINGS") && armor[1] == null) {
                            armor[1] = item;
                            continue;
                        }
                        if (typeName.endsWith("_BOOTS") && armor[0] == null) {
                            armor[0] = item;
                            continue;
                        }
                        int rawSlot = -1;
                        if (kitItem.getId() != null) {
                            try {
                                rawSlot = Integer.parseInt(kitItem.getId());
                            }
                            catch (NumberFormatException numberFormatException) {
                                // empty catch block
                            }
                        }
                        if (rawSlot == 40 && offHand == null) {
                            offHand = item;
                            continue;
                        }
                        if (rawSlot == 39 && armor[3] == null) {
                            armor[3] = item;
                            continue;
                        }
                        if (rawSlot == 38 && armor[2] == null) {
                            armor[2] = item;
                            continue;
                        }
                        if (rawSlot == 37 && armor[1] == null) {
                            armor[1] = item;
                            continue;
                        }
                        if (rawSlot == 36 && armor[0] == null) {
                            armor[0] = item;
                            continue;
                        }
                        if (rawSlot >= 0 && rawSlot < 36 && contents[rawSlot] == null) {
                            contents[rawSlot] = item;
                            continue;
                        }
                        for (int i = 0; i < contents.length; ++i) {
                            if (contents[i] != null) continue;
                            contents[i] = item;
                            continue block5;
                        }
                    }
                }
                if (contents[0] == null) {
                    int i;
                    ArrayList<ItemStack> nonNullItems = new ArrayList<ItemStack>();
                    for (i = 0; i < contents.length; ++i) {
                        if (contents[i] == null) continue;
                        nonNullItems.add(contents[i]);
                    }
                    if (!nonNullItems.isEmpty()) {
                        contents = new ItemStack[36];
                        for (i = 0; i < nonNullItems.size() && i < 36; ++i) {
                            contents[i] = (ItemStack)nonNullItems.get(i);
                        }
                    }
                }
                network.minespazio.spazioduels.kit.Kit kit = new network.minespazio.spazioduels.kit.Kit(kitName, icon, contents, armor, offHand, new ArrayList<PotionEffect>(), false, true, true);
                importedKits.add(kit);
            }
            this.plugin.getLogger().info("Se importaron " + importedKits.size() + " kits desde PlayerKits2.");
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("Error al importar kits desde PlayerKits2: " + t.getMessage());
        }
        return importedKits;
    }

    public boolean giveKitToPlayer(Player player, String kitName) {
        if (!this.enabled || player == null || kitName == null) {
            return false;
        }
        try {
            PlayerKits2 pkPlugin = PlayerKitsAPI.getPlugin();
            if (pkPlugin == null || pkPlugin.getKitsManager() == null) {
                return false;
            }
            GiveKitInstructions instructions = new GiveKitInstructions();
            instructions.setIgnorePermission(true);
            instructions.setIgnoreRequirements(true);
            instructions.setFromCommand(false);
            PlayerKitsMessageResult result = pkPlugin.getKitsManager().giveKit(player, kitName, instructions);
            return result != null && !result.isError();
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("Error al entregar kit de PlayerKits2 (" + kitName + ") a " + player.getName() + ": " + t.getMessage());
            return false;
        }
    }
}

