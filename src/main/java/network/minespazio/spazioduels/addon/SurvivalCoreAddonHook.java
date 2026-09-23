/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  network.minespazio.survivalcore.api.SurvivalCoreAddonDescriptor
 *  network.minespazio.survivalcore.api.SurvivalCoreApi
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.RegisteredServiceProvider
 */
package network.minespazio.spazioduels.addon;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.survivalcore.api.SurvivalCoreAddonDescriptor;
import network.minespazio.survivalcore.api.SurvivalCoreApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class SurvivalCoreAddonHook {
    private final SpazioDuelsPlugin plugin;
    private SurvivalCoreApi api;
    private boolean registered = false;

    public SurvivalCoreAddonHook(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        try {
            RegisteredServiceProvider registration = Bukkit.getServicesManager().getRegistration(SurvivalCoreApi.class);
            if (registration == null) {
                this.plugin.getLogger().warning("survival_core API no esta disponible en ServicesManager.");
                return;
            }
            this.api = (SurvivalCoreApi)registration.getProvider();
            this.api.addons().register(SurvivalCoreAddonDescriptor.of((String)"spazio-duels", (String)"SpazioDuels", (String)this.plugin.getPluginMeta().getVersion(), (String[])new String[]{"duels.core", "duels.events"}));
            this.registered = true;
            this.plugin.getLogger().info("Registrado exitosamente como Addon de survival_core!");
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("No se pudo registrar en survival_core API: " + t.getMessage());
        }
    }

    public void unregister() {
        if (this.registered && this.api != null) {
            try {
                this.api.addons().unregister("spazio-duels");
                this.plugin.getLogger().info("Addon espacio-duels desregistrado de survival_core.");
            }
            catch (Throwable t) {
                this.plugin.getLogger().warning("Error al desregistrar addon: " + t.getMessage());
            }
        }
    }

    public SurvivalCoreApi getApi() {
        return this.api;
    }
}

