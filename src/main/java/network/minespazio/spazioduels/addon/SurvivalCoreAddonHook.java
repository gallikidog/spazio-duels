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
            RegisteredServiceProvider<SurvivalCoreApi> registration =
                    Bukkit.getServicesManager().getRegistration(SurvivalCoreApi.class);

            if (registration == null) {
                plugin.getLogger().warning("survival_core API no esta disponible en ServicesManager.");
                return;
            }

            this.api = registration.getProvider();

            api.addons().register(SurvivalCoreAddonDescriptor.of(
                    "spazio-duels",
                    "SpazioDuels",
                    plugin.getPluginMeta().getVersion(),
                    "duels.core",
                    "duels.events"
            ));

            registered = true;
            plugin.getLogger().info("Registrado exitosamente como Addon de survival_core!");
        } catch (Throwable t) {
            plugin.getLogger().warning("No se pudo registrar en survival_core API: " + t.getMessage());
        }
    }

    public void unregister() {
        if (registered && api != null) {
            try {
                api.addons().unregister("spazio-duels");
                plugin.getLogger().info("Addon espacio-duels desregistrado de survival_core.");
            } catch (Throwable t) {
                plugin.getLogger().warning("Error al desregistrar addon: " + t.getMessage());
            }
        }
    }

    public SurvivalCoreApi getApi() {
        return api;
    }
}
