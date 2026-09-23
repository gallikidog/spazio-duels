/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package network.minespazio.spazioduels.arena;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaManager {
    private final SpazioDuelsPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<String, Arena>();
    private Location globalLobbySpawn;
    private File file;
    private YamlConfiguration config;

    public ArenaManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.initFile();
        this.loadArenas();
    }

    private void initFile() {
        this.file = new File(this.plugin.getDataFolder(), "arenas.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("arenas.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void loadArenas() {
        ConfigurationSection section;
        this.arenas.clear();
        if (this.config.contains("global_lobby")) {
            this.globalLobbySpawn = this.locationFromConfig(this.config.getConfigurationSection("global_lobby"));
        }
        if ((section = this.config.getConfigurationSection("arenas")) == null) {
            return;
        }
        for (String arenaName : section.getKeys(false)) {
            try {
                ConfigurationSection arenaSec = section.getConfigurationSection(arenaName);
                if (arenaSec == null) continue;
                Location spawn1 = this.locationFromConfig(arenaSec.getConfigurationSection("spawn1"));
                Location spawn2 = this.locationFromConfig(arenaSec.getConfigurationSection("spawn2"));
                Location spec = this.locationFromConfig(arenaSec.getConfigurationSection("spectator"));
                ArenaState state = ArenaState.valueOf(arenaSec.getString("state", "IN_SETUP"));
                int maxDuration = arenaSec.getInt("max_duration_seconds", 600);
                Arena arena = new Arena(arenaName, spawn1, spawn2, spec, state, maxDuration);
                this.arenas.put(arenaName.toLowerCase(), arena);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error al cargar la arena: " + arenaName, e);
            }
        }
        this.plugin.getLogger().info("Se cargaron " + this.arenas.size() + " arenas de duelos.");
    }

    public void saveArenas() {
        this.config.set("arenas", null);
        if (this.globalLobbySpawn != null) {
            this.locationToConfig(this.config.createSection("global_lobby"), this.globalLobbySpawn);
        }
        for (Arena arena : this.arenas.values()) {
            String path = "arenas." + arena.getName();
            if (arena.getSpawn1() != null) {
                this.locationToConfig(this.config.createSection(path + ".spawn1"), arena.getSpawn1());
            }
            if (arena.getSpawn2() != null) {
                this.locationToConfig(this.config.createSection(path + ".spawn2"), arena.getSpawn2());
            }
            if (arena.getSpectatorSpawn() != null) {
                this.locationToConfig(this.config.createSection(path + ".spectator"), arena.getSpectatorSpawn());
            }
            this.config.set(path + ".state", (Object)arena.getState().name());
            this.config.set(path + ".max_duration_seconds", (Object)arena.getMaxDurationSeconds());
        }
        try {
            this.config.save(this.file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar arenas.yml", e);
        }
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        this.arenas.put(name.toLowerCase(), arena);
        this.saveArenas();
        return arena;
    }

    public boolean deleteArena(String name) {
        if (this.arenas.remove(name.toLowerCase()) != null) {
            this.saveArenas();
            return true;
        }
        return false;
    }

    public Arena getArena(String name) {
        if (name == null) {
            return null;
        }
        return this.arenas.get(name.toLowerCase());
    }

    public Arena getAvailableArena() {
        for (Arena arena : this.arenas.values()) {
            if (arena.getState() != ArenaState.AVAILABLE || !arena.isReady()) continue;
            return arena;
        }
        return null;
    }

    public Collection<Arena> getArenas() {
        return this.arenas.values();
    }

    public Location getGlobalLobbySpawn() {
        return this.globalLobbySpawn;
    }

    public void setGlobalLobbySpawn(Location globalLobbySpawn) {
        this.globalLobbySpawn = globalLobbySpawn;
        this.saveArenas();
    }

    private Location locationFromConfig(ConfigurationSection sec) {
        if (sec == null) {
            return null;
        }
        String worldName = sec.getString("world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld((String)worldName);
        if (world == null) {
            return null;
        }
        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float)sec.getDouble("yaw");
        float pitch = (float)sec.getDouble("pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void locationToConfig(ConfigurationSection sec, Location loc) {
        if (sec == null || loc == null || loc.getWorld() == null) {
            return;
        }
        sec.set("world", (Object)loc.getWorld().getName());
        sec.set("x", (Object)loc.getX());
        sec.set("y", (Object)loc.getY());
        sec.set("z", (Object)loc.getZ());
        sec.set("yaw", (Object)Float.valueOf(loc.getYaw()));
        sec.set("pitch", (Object)Float.valueOf(loc.getPitch()));
    }
}

