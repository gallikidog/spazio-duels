package network.minespazio.spazioduels.integration;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pk.ajneb97.PlayerKits2;
import pk.ajneb97.api.PlayerKitsAPI;
import pk.ajneb97.model.item.KitItem;

import java.util.ArrayList;
import java.util.List;

public class PlayerKitsHook {

    private final SpazioDuelsPlugin plugin;
    private boolean enabled;

    public PlayerKitsHook(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("PlayerKits2");
        if (enabled) {
            plugin.getLogger().info("PlayerKits2 detectado. Se habilitará la integración automática de kits.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Kit> importKitsFromPlayerKits() {
        List<Kit> importedKits = new ArrayList<>();
        if (!enabled) return importedKits;

        try {
            PlayerKits2 pkPlugin = PlayerKitsAPI.getPlugin();
            if (pkPlugin == null || pkPlugin.getKitsManager() == null) return importedKits;

            ArrayList<pk.ajneb97.model.Kit> pkKits = pkPlugin.getKitsManager().getKits();
            if (pkKits == null) return importedKits;

            for (pk.ajneb97.model.Kit pkKit : pkKits) {
                String kitName = pkKit.getName();

                // Icon item
                ItemStack icon = null;
                if (pkKit.getDisplayItemDefault() != null) {
                    icon = pkPlugin.getKitItemManager().createItemFromKitItem(pkKit.getDisplayItemDefault(), null, pkKit);
                }
                if (icon == null || icon.getType() == Material.AIR) {
                    icon = new ItemStack(Material.DIAMOND_SWORD);
                }

                // Inventory & armor contents
                ItemStack[] contents = new ItemStack[36];
                ItemStack[] armor = new ItemStack[4]; // 0: boots, 1: leggings, 2: chestplate, 3: helmet
                ItemStack offHand = null;

                if (pkKit.getItems() != null) {
                    for (KitItem kitItem : pkKit.getItems()) {
                        ItemStack item = pkPlugin.getKitItemManager().createItemFromKitItem(kitItem, null, pkKit);
                        if (item == null || item.getType() == Material.AIR) continue;

                        if (kitItem.isOffhand()) {
                            offHand = item;
                            continue;
                        }

                        int slot = -1;
                        if (kitItem.getId() != null) {
                            try {
                                slot = Integer.parseInt(kitItem.getId());
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        if (slot >= 0 && slot < 36) {
                            contents[slot] = item;
                        } else if (slot == 36) {
                            armor[0] = item; // Boots
                        } else if (slot == 37) {
                            armor[1] = item; // Leggings
                        } else if (slot == 38) {
                            armor[2] = item; // Chestplate
                        } else if (slot == 39) {
                            armor[3] = item; // Helmet
                        } else if (slot == 40) {
                            offHand = item;
                        } else {
                            // Fallback logic for unmapped slots or material types
                            Material type = item.getType();
                            String typeName = type.name();

                            if (typeName.endsWith("_HELMET") && armor[3] == null) {
                                armor[3] = item;
                            } else if (typeName.endsWith("_CHESTPLATE") && armor[2] == null) {
                                armor[2] = item;
                            } else if (typeName.endsWith("_LEGGINGS") && armor[1] == null) {
                                armor[1] = item;
                            } else if (typeName.endsWith("_BOOTS") && armor[0] == null) {
                                armor[0] = item;
                            } else {
                                // Place in first empty slot in contents
                                for (int i = 0; i < contents.length; i++) {
                                    if (contents[i] == null) {
                                        contents[i] = item;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                Kit kit = new Kit(kitName, icon, contents, armor, offHand, new ArrayList<>(), false, true, true);
                importedKits.add(kit);
            }
            plugin.getLogger().info("Se importaron " + importedKits.size() + " kits desde PlayerKits2.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Error al importar kits desde PlayerKits2: " + t.getMessage());
        }

        return importedKits;
    }
}
