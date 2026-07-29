package network.minespazio.spazioduels.koth;

import network.minespazio.spazioduels.SpazioDuelsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class KothManager {

    private final SpazioDuelsPlugin plugin;
    private final Map<String, Koth> koths = new LinkedHashMap<>();
    private KothMatch activeMatch;
    private File file;
    private YamlConfiguration config;

    public KothManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        initFile();
        loadKoths();
        startAutoSchedule();
    }

    private void initFile() {
        file = new File(plugin.getDataFolder(), "koths.yml");
        if (!file.exists()) {
            plugin.saveResource("koths.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void loadKoths() {
        koths.clear();
        ConfigurationSection section = config.getConfigurationSection("koths");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            try {
                ConfigurationSection kothSec = section.getConfigurationSection(name);
                if (kothSec == null) continue;

                int delay = kothSec.getInt("capture_delay_seconds", 300);
                CuboidRegion zone = CuboidRegion.fromConfig(kothSec.getConfigurationSection("zone"));
                CuboidRegion capZone = CuboidRegion.fromConfig(kothSec.getConfigurationSection("cap_zone"));

                List<?> lootList = kothSec.getList("loot");
                List<ItemStack> loot = new ArrayList<>();
                if (lootList != null) {
                    for (Object obj : lootList) {
                        if (obj instanceof ItemStack item) {
                            loot.add(item);
                        }
                    }
                }

                Koth koth = new Koth(name, delay, zone, capZone, loot);
                koths.put(name.toLowerCase(), koth);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el KOTH: " + name, e);
            }
        }
        plugin.getLogger().info("Se cargaron " + koths.size() + " KOTHs.");
    }

    public void saveKoths() {
        config.set("koths", null);
        for (Koth koth : koths.values()) {
            String path = "koths." + koth.getName();
            config.set(path + ".capture_delay_seconds", koth.getCaptureDelaySeconds());
            if (koth.getZone() != null) koth.getZone().toConfig(config.createSection(path + ".zone"));
            if (koth.getCapZone() != null) koth.getCapZone().toConfig(config.createSection(path + ".cap_zone"));
            config.set(path + ".loot", koth.getLootItems());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar koths.yml", e);
        }
    }

    public Koth createKoth(String name) {
        Koth koth = new Koth(name);
        koths.put(name.toLowerCase(), koth);
        saveKoths();
        return koth;
    }

    public Koth getKoth(String name) {
        if (name == null) return null;
        return koths.get(name.toLowerCase());
    }

    public Collection<Koth> getKoths() {
        return koths.values();
    }

    public boolean deleteKoth(String name) {
        if (koths.remove(name.toLowerCase()) != null) {
            saveKoths();
            return true;
        }
        return false;
    }

    public boolean startKoth(String name) {
        Koth koth = getKoth(name);
        if (koth == null || !koth.isReady()) return false;
        if (activeMatch != null && activeMatch.isActive()) {
            stopActiveMatch();
        }

        activeMatch = new KothMatch(plugin, koth);
        activeMatch.start();
        return true;
    }

    public void stopActiveMatch() {
        if (activeMatch != null) {
            if (activeMatch.isActive()) {
                activeMatch.stopManually();
            }
            activeMatch = null;
        }
    }

    public KothMatch getActiveMatch() {
        return activeMatch;
    }

    public CuboidRegion getPlayerWorldEditSelection(Player player) {
        Plugin wePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) wePlugin = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");

        if (wePlugin != null) {
            try {
                com.sk89q.worldedit.bukkit.WorldEditPlugin we = (com.sk89q.worldedit.bukkit.WorldEditPlugin) wePlugin;
                com.sk89q.worldedit.regions.Region selection = we.getSession(player).getSelection(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getWorld()));
                if (selection != null) {
                    com.sk89q.worldedit.math.BlockVector3 min = selection.getMinimumPoint();
                    com.sk89q.worldedit.math.BlockVector3 max = selection.getMaximumPoint();
                    return new CuboidRegion(player.getWorld().getName(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("No se pudo obtener la selección de WorldEdit: " + t.getMessage());
            }
        }
        return null;
    }

    private void startAutoSchedule() {
        if (!plugin.getConfig().getBoolean("koth_autostart.enabled", false)) return;

        int intervalMinutes = plugin.getConfig().getInt("koth_autostart.interval_minutes", 120);
        long ticks = intervalMinutes * 60 * 20L;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeMatch != null && activeMatch.isActive()) return;

                List<Koth> readyKoths = new ArrayList<>();
                for (Koth k : koths.values()) {
                    if (k.isReady()) readyKoths.add(k);
                }

                if (!readyKoths.isEmpty()) {
                    Koth randomKoth = readyKoths.get(new Random().nextInt(readyKoths.size()));
                    startKoth(randomKoth.getName());
                }
            }
        }.runTaskTimer(plugin, ticks, ticks);
    }
}
