package network.minespazio.spazioduels;

import network.minespazio.spazioduels.addon.SurvivalCoreAddonHook;
import network.minespazio.spazioduels.antidupe.AntiDupeManager;
import network.minespazio.spazioduels.antidupe.InventoryBackupManager;
import network.minespazio.spazioduels.arena.ArenaManager;
import network.minespazio.spazioduels.command.*;
import network.minespazio.spazioduels.duel.DuelManager;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.event.DuelEventManager;
import network.minespazio.spazioduels.kit.KitManager;
import network.minespazio.spazioduels.koth.KothManager;
import network.minespazio.spazioduels.listener.AntiDupeListener;
import network.minespazio.spazioduels.listener.GUIListener;
import network.minespazio.spazioduels.listener.MatchListener;
import network.minespazio.spazioduels.listener.ProtectionListener;
import network.minespazio.spazioduels.party.PartyManager;
import network.minespazio.spazioduels.placeholder.SpazioDuelsPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public final class SpazioDuelsPlugin extends JavaPlugin {

    private SurvivalCoreAddonHook addonHook;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private AntiDupeManager antiDupeManager;
    private InventoryBackupManager inventoryBackupManager;
    private PartyManager partyManager;
    private DuelManager duelManager;
    private DuelEventManager duelEventManager;
    private KothManager kothManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        this.kitManager = new KitManager(this);
        this.arenaManager = new ArenaManager(this);
        this.antiDupeManager = new AntiDupeManager(this);
        this.inventoryBackupManager = new InventoryBackupManager(this);
        this.partyManager = new PartyManager(this);
        this.duelManager = new DuelManager(this);
        this.duelEventManager = new DuelEventManager(this);
        this.kothManager = new KothManager(this);

        // Register survival_core addon hook
        this.addonHook = new SurvivalCoreAddonHook(this);
        this.addonHook.register();

        // Register listeners
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);
        getServer().getPluginManager().registerEvents(new AntiDupeListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Register commands
        if (getCommand("duel") != null) {
            DuelCommand cmd = new DuelCommand(this);
            getCommand("duel").setExecutor(cmd);
            getCommand("duel").setTabCompleter(cmd);
        }
        if (getCommand("duelevent") != null) {
            DuelEventCommand cmd = new DuelEventCommand(this);
            getCommand("duelevent").setExecutor(cmd);
            getCommand("duelevent").setTabCompleter(cmd);
        }
        if (getCommand("spazioduels") != null) {
            SpazioDuelsAdminCommand cmd = new SpazioDuelsAdminCommand(this);
            getCommand("spazioduels").setExecutor(cmd);
            getCommand("spazioduels").setTabCompleter(cmd);
        }
        if (getCommand("koth") != null) {
            KothCommand cmd = new KothCommand(this);
            getCommand("koth").setExecutor(cmd);
            getCommand("koth").setTabCompleter(cmd);
        }
        if (getCommand("party") != null) {
            PartyCommand cmd = new PartyCommand(this);
            getCommand("party").setExecutor(cmd);
            getCommand("party").setTabCompleter(cmd);
        }

        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SpazioDuelsPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI Expansion para SpazioDuels registrada!");
        }

        getLogger().info("SpazioDuels v" + getPluginMeta().getVersion() + " habilitado exitosamente.");
    }

    @Override
    public void onDisable() {
        // Force cleanup of any active matches
        if (duelManager != null) {
            for (DuelMatch match : new HashSet<>(duelManager.getActiveMatches())) {
                match.cleanupAndRestore();
            }
        }

        if (kothManager != null) {
            kothManager.stopActiveMatch();
        }

        // Unregister survival_core addon hook
        if (addonHook != null) {
            addonHook.unregister();
        }

        getLogger().info("SpazioDuels deshabilitado.");
    }

    public SurvivalCoreAddonHook getAddonHook() {
        return addonHook;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public AntiDupeManager getAntiDupeManager() {
        return antiDupeManager;
    }

    public InventoryBackupManager getInventoryBackupManager() {
        return inventoryBackupManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public DuelEventManager getDuelEventManager() {
        return duelEventManager;
    }

    public KothManager getKothManager() {
        return kothManager;
    }
}
