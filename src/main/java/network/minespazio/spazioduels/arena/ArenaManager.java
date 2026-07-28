package network.minespazio.spazioduels.arena;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ArenaManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private Location globalLobbySpawn;
    private File file;
    private YamlConfiguration config;

    public ArenaManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        initFile();
        loadArenas();
    }

    private void initFile() {
        file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadArenas() {
        arenas.clear();
        if (config.contains("global_lobby")) {
            globalLobbySpawn = locationFromConfig(config.getConfigurationSection("global_lobby"));
        }

        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String arenaName : section.getKeys(false)) {
            try {
                ConfigurationSection arenaSec = section.getConfigurationSection(arenaName);
                if (arenaSec == null) continue;

                Location spawn1 = locationFromConfig(arenaSec.getConfigurationSection("spawn1"));
                Location spawn2 = locationFromConfig(arenaSec.getConfigurationSection("spawn2"));
                Location spec = locationFromConfig(arenaSec.getConfigurationSection("spectator"));
                ArenaState state = ArenaState.valueOf(arenaSec.getString("state", "IN_SETUP"));
                int maxDuration = arenaSec.getInt("max_duration_seconds", 600);

                Arena arena = new Arena(arenaName, spawn1, spawn2, spec, state, maxDuration);
                arenas.put(arenaName.toLowerCase(), arena);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar la arena: " + arenaName, e);
            }
        }
        plugin.getLogger().info("Se cargaron " + arenas.size() + " arenas de duelos.");
    }

    public void saveArenas() {
        config.set("arenas", null);
        if (globalLobbySpawn != null) {
            locationToConfig(config.createSection("global_lobby"), globalLobbySpawn);
        }

        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            if (arena.getSpawn1() != null) locationToConfig(config.createSection(path + ".spawn1"), arena.getSpawn1());
            if (arena.getSpawn2() != null) locationToConfig(config.createSection(path + ".spawn2"), arena.getSpawn2());
            if (arena.getSpectatorSpawn() != null) locationToConfig(config.createSection(path + ".spectator"), arena.getSpectatorSpawn());
            config.set(path + ".state", arena.getState().name());
            config.set(path + ".max_duration_seconds", arena.getMaxDurationSeconds());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar arenas.yml", e);
        }
    }

    public Arena createArena(String name) {
        Arena arena = new Arena(name);
        arenas.put(name.toLowerCase(), arena);
        saveArenas();
        return arena;
    }

    public boolean deleteArena(String name) {
        if (arenas.remove(name.toLowerCase()) != null) {
            saveArenas();
            return true;
        }
        return false;
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public Arena getAvailableArena() {
        for (Arena arena : arenas.values()) {
            if (arena.getState() == ArenaState.AVAILABLE && arena.isReady()) {
                return arena;
            }
        }
        return null;
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public Location getGlobalLobbySpawn() {
        return globalLobbySpawn;
    }

    public void setGlobalLobbySpawn(Location globalLobbySpawn) {
        this.globalLobbySpawn = globalLobbySpawn;
        saveArenas();
    }

    private Location locationFromConfig(ConfigurationSection sec) {
        if (sec == null) return null;
        String worldName = sec.getString("world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = sec.getDouble("x");
        double y = sec.getDouble("y");
        double z = sec.getDouble("z");
        float yaw = (float) sec.getDouble("yaw");
        float pitch = (float) sec.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void locationToConfig(ConfigurationSection sec, Location loc) {
        if (sec == null || loc == null || loc.getWorld() == null) return;
        sec.set("world", loc.getWorld().getName());
        sec.set("x", loc.getX());
        sec.set("y", loc.getY());
        sec.set("z", loc.getZ());
        sec.set("yaw", loc.getYaw());
        sec.set("pitch", loc.getPitch());
    }
}
