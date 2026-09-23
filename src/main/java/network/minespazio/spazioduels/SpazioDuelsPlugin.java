/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package network.minespazio.spazioduels;

import java.util.HashSet;
import network.minespazio.spazioduels.addon.SurvivalCoreAddonHook;
import network.minespazio.spazioduels.antidupe.AntiDupeManager;
import network.minespazio.spazioduels.antidupe.InventoryBackupManager;
import network.minespazio.spazioduels.arena.ArenaManager;
import network.minespazio.spazioduels.command.DuelCommand;
import network.minespazio.spazioduels.command.DuelEventCommand;
import network.minespazio.spazioduels.command.KothCommand;
import network.minespazio.spazioduels.command.PartyCommand;
import network.minespazio.spazioduels.command.SpazioDuelsAdminCommand;
import network.minespazio.spazioduels.duel.DuelManager;
import network.minespazio.spazioduels.duel.DuelMatch;
import network.minespazio.spazioduels.event.DuelEventManager;
import network.minespazio.spazioduels.kit.KitManager;
import network.minespazio.spazioduels.koth.KothManager;
import network.minespazio.spazioduels.listener.AntiDupeListener;
import network.minespazio.spazioduels.listener.GUIListener;
import network.minespazio.spazioduels.listener.MatchListener;
import network.minespazio.spazioduels.listener.ProtectionListener;
import network.minespazio.spazioduels.listener.SoupPvPListener;
import network.minespazio.spazioduels.party.PartyManager;
import network.minespazio.spazioduels.placeholder.SpazioDuelsPlaceholderExpansion;
import network.minespazio.spazioduels.pvp.PvP18Listener;
import network.minespazio.spazioduels.pvp.PvP18Manager;
import network.minespazio.spazioduels.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpazioDuelsPlugin
extends JavaPlugin {
    private SurvivalCoreAddonHook addonHook;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private AntiDupeManager antiDupeManager;
    private InventoryBackupManager inventoryBackupManager;
    private PartyManager partyManager;
    private DuelManager duelManager;
    private DuelEventManager duelEventManager;
    private KothManager kothManager;
    private ScoreboardManager scoreboardManager;
    private PvP18Manager pvp18Manager;

    public void onEnable() {
        Object cmd;
        this.saveDefaultConfig();
        this.kitManager = new KitManager(this);
        this.arenaManager = new ArenaManager(this);
        this.antiDupeManager = new AntiDupeManager(this);
        this.inventoryBackupManager = new InventoryBackupManager(this);
        this.partyManager = new PartyManager(this);
        this.duelManager = new DuelManager(this);
        this.duelEventManager = new DuelEventManager(this);
        this.kothManager = new KothManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.pvp18Manager = new PvP18Manager(this);
        this.addonHook = new SurvivalCoreAddonHook(this);
        this.addonHook.register();
        this.getServer().getPluginManager().registerEvents((Listener)new ProtectionListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new MatchListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new AntiDupeListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new GUIListener(this), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new PvP18Listener(this, this.pvp18Manager), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new SoupPvPListener(this), (Plugin)this);
        if (this.getCommand("duel") != null) {
            cmd = new DuelCommand(this);
            this.getCommand("duel").setExecutor((CommandExecutor)cmd);
            this.getCommand("duel").setTabCompleter((TabCompleter)cmd);
        }
        if (this.getCommand("duelevent") != null) {
            cmd = new DuelEventCommand(this);
            this.getCommand("duelevent").setExecutor((CommandExecutor)cmd);
            this.getCommand("duelevent").setTabCompleter((TabCompleter)cmd);
        }
        if (this.getCommand("spazioduels") != null) {
            cmd = new SpazioDuelsAdminCommand(this);
            this.getCommand("spazioduels").setExecutor((CommandExecutor)cmd);
            this.getCommand("spazioduels").setTabCompleter((TabCompleter)cmd);
        }
        if (this.getCommand("koth") != null) {
            cmd = new KothCommand(this);
            this.getCommand("koth").setExecutor((CommandExecutor)cmd);
            this.getCommand("koth").setTabCompleter((TabCompleter)cmd);
        }
        if (this.getCommand("party") != null) {
            cmd = new PartyCommand(this);
            this.getCommand("party").setExecutor((CommandExecutor)cmd);
            this.getCommand("party").setTabCompleter((TabCompleter)cmd);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SpazioDuelsPlaceholderExpansion(this).register();
            this.getLogger().info("PlaceholderAPI Expansion para SpazioDuels registrada!");
        }
        this.getLogger().info("SpazioDuels v" + this.getPluginMeta().getVersion() + " habilitado exitosamente.");
    }

    public void onDisable() {
        if (this.duelManager != null) {
            for (DuelMatch match : new HashSet<DuelMatch>(this.duelManager.getActiveMatches())) {
                match.cleanupAndRestore();
            }
        }
        if (this.kothManager != null) {
            this.kothManager.stopActiveMatch();
        }
        if (this.scoreboardManager != null) {
            this.scoreboardManager.clearAllBoards();
        }
        if (this.addonHook != null) {
            this.addonHook.unregister();
        }
        this.getLogger().info("SpazioDuels deshabilitado.");
    }

    public SurvivalCoreAddonHook getAddonHook() {
        return this.addonHook;
    }

    public KitManager getKitManager() {
        return this.kitManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public AntiDupeManager getAntiDupeManager() {
        return this.antiDupeManager;
    }

    public InventoryBackupManager getInventoryBackupManager() {
        return this.inventoryBackupManager;
    }

    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    public DuelManager getDuelManager() {
        return this.duelManager;
    }

    public DuelEventManager getDuelEventManager() {
        return this.duelEventManager;
    }

    public KothManager getKothManager() {
        return this.kothManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public PvP18Manager getPvP18Manager() {
        return this.pvp18Manager;
    }

    public void reloadDuelConfiguration() {
        this.reloadConfig();
        this.kitManager.loadKits();
        this.arenaManager.loadArenas();
        if (this.scoreboardManager != null) {
            this.scoreboardManager.reloadConfig();
        }
    }
}

