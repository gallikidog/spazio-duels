/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldedit.bukkit.BukkitAdapter
 *  com.sk89q.worldedit.bukkit.WorldEditPlugin
 *  com.sk89q.worldedit.math.BlockVector3
 *  com.sk89q.worldedit.regions.Region
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package network.minespazio.spazioduels.koth;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothCommandReward;
import network.minespazio.spazioduels.koth.KothMatch;
import network.minespazio.spazioduels.koth.KothSchedule;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class KothManager {
    private final SpazioDuelsPlugin plugin;
    private final Map<String, Koth> koths = new LinkedHashMap<String, Koth>();
    private KothMatch activeMatch;
    private final Map<String, String> scheduledActivationMinutes = new LinkedHashMap<String, String>();
    private File file;
    private YamlConfiguration config;

    public KothManager(SpazioDuelsPlugin plugin) {
        this.plugin = plugin;
        this.initFile();
        this.loadKoths();
        this.startAutoSchedule();
    }

    private void initFile() {
        this.file = new File(this.plugin.getDataFolder(), "koths.yml");
        if (!this.file.exists()) {
            this.plugin.saveResource("koths.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
    }

    public void loadKoths() {
        this.koths.clear();
        this.config = YamlConfiguration.loadConfiguration((File)this.file);
        ConfigurationSection section = this.config.getConfigurationSection("koths");
        if (section == null) {
            return;
        }
        boolean migrated = false;
        for (String name : section.getKeys(false)) {
            try {
                ConfigurationSection kothSec = section.getConfigurationSection(name);
                if (kothSec == null) continue;
                migrated |= this.migrateKothConfiguration(kothSec, name);
                int delay = kothSec.getInt("capture_delay_seconds", 300);
                CuboidRegion zone = CuboidRegion.fromConfig(kothSec.getConfigurationSection("zone"));
                CuboidRegion capZone = CuboidRegion.fromConfig(kothSec.getConfigurationSection("cap_zone"));
                List lootList = kothSec.getList("loot");
                ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
                if (lootList != null) {
                    for (Object obj : lootList) {
                        if (!(obj instanceof ItemStack)) continue;
                        ItemStack item = (ItemStack)obj;
                        loot.add(item);
                    }
                }
                List<KothCommandReward> commandRewards = this.loadCommandRewards(kothSec.getList("command_rewards"));
                List activationTimes = kothSec.getStringList("activation_times");
                Koth koth = new Koth(name, delay, zone, capZone, loot, commandRewards, activationTimes);
                this.koths.put(name.toLowerCase(), koth);
            }
            catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error al cargar el KOTH: " + name, e);
            }
        }
        if (migrated) {
            try {
                this.config.save(this.file);
                this.plugin.getLogger().info("Se adapto koths.yml al formato actual de recompensas KOTH.");
            }
            catch (IOException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar la migracion de koths.yml", exception);
            }
        }
        this.plugin.getLogger().info("Se cargaron " + this.koths.size() + " KOTHs.");
    }

    public void saveKoths() {
        this.config.set("koths", null);
        for (Koth koth : this.koths.values()) {
            String path = "koths." + koth.getName();
            this.config.set(path + ".capture_delay_seconds", (Object)koth.getCaptureDelaySeconds());
            if (koth.getZone() != null) {
                koth.getZone().toConfig(this.config.createSection(path + ".zone"));
            }
            if (koth.getCapZone() != null) {
                koth.getCapZone().toConfig(this.config.createSection(path + ".cap_zone"));
            }
            this.config.set(path + ".loot", koth.getLootItems());
            this.config.set(path + ".command_rewards", this.saveCommandRewards(koth));
            this.config.set(path + ".activation_times", koth.getActivationTimes());
        }
        try {
            this.config.save(this.file);
        }
        catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "No se pudo guardar koths.yml", e);
        }
    }

    public Koth createKoth(String name) {
        Koth koth = new Koth(name);
        this.koths.put(name.toLowerCase(), koth);
        this.saveKoths();
        return koth;
    }

    private List<KothCommandReward> loadCommandRewards(List<?> rawRewards) {
        ArrayList<KothCommandReward> rewards = new ArrayList<KothCommandReward>();
        if (rawRewards == null) {
            return rewards;
        }
        for (Object rawReward : rawRewards) {
            if (rawReward instanceof String) {
                String command = (String)rawReward;
                if (command.isBlank()) continue;
                rewards.add(new KothCommandReward(command, 100.0, true));
                continue;
            }
            if (!(rawReward instanceof Map)) continue;
            Map map = (Map)rawReward;
            Object commandValue = map.get("command");
            Object chanceValue = map.get("chance");
            if (!(commandValue instanceof String) || chanceValue == null) continue;
            try {
                double chance = chanceValue instanceof Number ? ((Number)chanceValue).doubleValue() : Double.parseDouble(String.valueOf(chanceValue));
                rewards.add(new KothCommandReward((String)commandValue, chance, false));
            }
            catch (NumberFormatException exception) {
                this.plugin.getLogger().warning("Se ignoro una recompensa KOTH con probabilidad invalida.");
            }
        }
        return rewards;
    }

    private boolean migrateKothConfiguration(ConfigurationSection kothSec, String name) {
        boolean changed = false;
        if (!kothSec.contains("loot")) {
            kothSec.set("loot", new ArrayList());
            changed = true;
        }
        if (!kothSec.contains("activation_times")) {
            kothSec.set("activation_times", new ArrayList());
            changed = true;
        }
        if (!kothSec.contains("command_rewards")) {
            kothSec.set("command_rewards", new ArrayList());
            return true;
        }
        List rawRewards = kothSec.getList("command_rewards");
        if (rawRewards == null || rawRewards.isEmpty()) {
            return changed;
        }
        ArrayList<CommandRewardMigration> entries = new ArrayList<CommandRewardMigration>();
        boolean needsMigration = false;
        for (Object rawReward : rawRewards) {
            if (rawReward instanceof String) {
                String command = (String)rawReward;
                if (!command.isBlank()) {
                    entries.add(new CommandRewardMigration(command, null));
                }
                needsMigration = true;
                continue;
            }
            if (rawReward instanceof Map map) {
                Object command = map.get("command");
                Double chance = this.readChance(map.get("chance"));
                if (command instanceof String cmdStr && !cmdStr.isBlank()) {
                    entries.add(new CommandRewardMigration(cmdStr, chance));
                    if (chance != null && !(chance <= 0.0) && !(chance > 100.0)) continue;
                    needsMigration = true;
                    continue;
                }
                needsMigration = true;
                continue;
            }
            needsMigration = true;
        }
        if (entries.size() > 3) {
            entries.subList(3, entries.size()).clear();
            needsMigration = true;
            this.plugin.getLogger().warning("El KOTH " + name + " tenia mas de 3 recompensas por comando; se conservaron las primeras tres.");
        }
        double configuredTotal = 0.0;
        ArrayList<CommandRewardMigration> missingChance = new ArrayList<CommandRewardMigration>();
        for (CommandRewardMigration entry : entries) {
            if (entry.chance == null || entry.chance <= 0.0 || entry.chance > 100.0) {
                missingChance.add(entry);
                continue;
            }
            configuredTotal += entry.chance.doubleValue();
        }
        if (configuredTotal > 100.000001) {
            for (CommandRewardMigration entry : entries) {
                entry.chance = null;
            }
            missingChance.clear();
            missingChance.addAll(entries);
            configuredTotal = 0.0;
            needsMigration = true;
        }
        if (!missingChance.isEmpty() && 100.0 - configuredTotal < 0.01 * (double)missingChance.size()) {
            for (CommandRewardMigration entry : entries) {
                entry.chance = null;
            }
            missingChance.clear();
            missingChance.addAll(entries);
            configuredTotal = 0.0;
            needsMigration = true;
            this.plugin.getLogger().warning("Se regeneraron las probabilidades del KOTH " + name + " porque no quedaba porcentaje para recompensas sin chance.");
        }
        if (!missingChance.isEmpty()) {
            this.assignRandomChances(missingChance, 100.0 - configuredTotal);
            needsMigration = true;
        }
        if (!needsMigration) {
            return changed;
        }
        ArrayList migratedRewards = new ArrayList();
        for (CommandRewardMigration entry : entries) {
            LinkedHashMap<String, Object> reward = new LinkedHashMap<String, Object>();
            reward.put("command", entry.command);
            reward.put("chance", entry.chance);
            migratedRewards.add(reward);
        }
        kothSec.set("command_rewards", migratedRewards);
        this.plugin.getLogger().info("Se adaptaron las recompensas por comando del KOTH " + name + ".");
        return true;
    }

    private Double readChance(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return value instanceof Number ? ((Number)value).doubleValue() : Double.parseDouble(String.valueOf(value));
        }
        catch (NumberFormatException exception) {
            return null;
        }
    }

    private void assignRandomChances(List<CommandRewardMigration> entries, double total) {
        if (entries.isEmpty()) {
            return;
        }
        double remaining = Math.max(0.0, total);
        for (int index = 0; index < entries.size(); ++index) {
            double chance;
            int left = entries.size() - index;
            if (left == 1) {
                chance = remaining;
            } else {
                double minimumForOthers = 0.01 * (double)(left - 1);
                double maximum = Math.max(0.01, remaining - minimumForOthers);
                chance = Math.min(maximum, Math.max(0.01, ThreadLocalRandom.current().nextDouble(0.01, maximum + 1.0E-6)));
            }
            chance = (double)Math.round(chance * 100.0) / 100.0;
            entries.get((int)index).chance = chance;
            remaining = Math.max(0.0, remaining - chance);
        }
    }

    private List<Object> saveCommandRewards(Koth koth) {
        ArrayList<Object> serialized = new ArrayList<Object>();
        for (KothCommandReward reward : koth.getCommandRewards()) {
            if (reward.guaranteed()) {
                serialized.add(reward.command());
                continue;
            }
            LinkedHashMap<String, Object> entry = new LinkedHashMap<String, Object>();
            entry.put("command", reward.command());
            entry.put("chance", reward.chance());
            serialized.add(entry);
        }
        return serialized;
    }

    public Koth getKoth(String name) {
        if (name == null) {
            return null;
        }
        return this.koths.get(name.toLowerCase());
    }

    public Collection<Koth> getKoths() {
        return this.koths.values();
    }

    public boolean deleteKoth(String name) {
        if (this.koths.remove(name.toLowerCase()) != null) {
            this.saveKoths();
            return true;
        }
        return false;
    }

    public boolean startKoth(String name) {
        Koth koth = this.getKoth(name);
        if (koth == null || !koth.isReady()) {
            return false;
        }
        if (this.activeMatch != null && this.activeMatch.isActive()) {
            this.stopActiveMatch();
        }
        this.activeMatch = new KothMatch(this.plugin, koth);
        this.activeMatch.start();
        return true;
    }

    public void stopActiveMatch() {
        if (this.activeMatch != null) {
            if (this.activeMatch.isActive()) {
                this.activeMatch.stopManually();
            }
            this.activeMatch = null;
        }
    }

    public KothMatch getActiveMatch() {
        return this.activeMatch;
    }

    public boolean isActiveKoth(String name) {
        return this.activeMatch != null && this.activeMatch.isActive() && this.activeMatch.getKoth().getName().equalsIgnoreCase(name);
    }

    public String getNextActivationCountdown(Koth koth) {
        LocalDateTime now = LocalDateTime.now();
        return KothSchedule.formatCountdown(KothSchedule.getNextActivation(koth.getActivationTimes(), now), now);
    }

    public String getNextActivationTime(Koth koth) {
        LocalDateTime nextActivation = KothSchedule.getNextActivation(koth.getActivationTimes(), LocalDateTime.now());
        return nextActivation == null ? "No programado" : String.format("%02d:%02d", nextActivation.getHour(), nextActivation.getMinute());
    }

    public CuboidRegion getPlayerWorldEditSelection(Player player) {
        Plugin wePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) {
            wePlugin = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        }
        if (wePlugin != null) {
            try {
                WorldEditPlugin we = (WorldEditPlugin)wePlugin;
                Region selection = we.getSession(player).getSelection(BukkitAdapter.adapt((World)player.getWorld()));
                if (selection != null) {
                    BlockVector3 min = selection.getMinimumPoint();
                    BlockVector3 max = selection.getMaximumPoint();
                    return new CuboidRegion(player.getWorld().getName(), min.getBlockX(), min.getBlockY(), min.getBlockZ(), max.getBlockX(), max.getBlockY(), max.getBlockZ());
                }
            }
            catch (Throwable t) {
                this.plugin.getLogger().warning("No se pudo obtener la selecci\u00f3n de WorldEdit: " + t.getMessage());
            }
        }
        return null;
    }

    private void startAutoSchedule() {
        new BukkitRunnable(){

            public void run() {
                KothManager.this.startScheduledKoths();
            }
        }.runTaskTimer((Plugin)this.plugin, 20L, 20L);
        if (!this.plugin.getConfig().getBoolean("koth_autostart.enabled", false)) {
            return;
        }
        int intervalMinutes = this.plugin.getConfig().getInt("koth_autostart.interval_minutes", 120);
        long ticks = (long)(intervalMinutes * 60) * 20L;
        new BukkitRunnable(){

            public void run() {
                if (KothManager.this.activeMatch != null && KothManager.this.activeMatch.isActive()) {
                    return;
                }
                ArrayList<Koth> readyKoths = new ArrayList<Koth>();
                for (Koth k : KothManager.this.koths.values()) {
                    if (!k.isReady() || !k.getActivationTimes().isEmpty()) continue;
                    readyKoths.add(k);
                }
                if (!readyKoths.isEmpty()) {
                    Koth randomKoth = (Koth)readyKoths.get(new Random().nextInt(readyKoths.size()));
                    KothManager.this.startKoth(randomKoth.getName());
                }
            }
        }.runTaskTimer((Plugin)this.plugin, ticks, ticks);
    }

    private void startScheduledKoths() {
        LocalDateTime now = LocalDateTime.now();
        String minuteKey = String.format("%04d-%02d-%02dT%02d:%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute());
        for (Koth koth : this.koths.values()) {
            String scheduleKey;
            if (!koth.isReady() || !koth.getActivationTimes().contains(String.format("%02d:%02d", now.getHour(), now.getMinute())) || minuteKey.equals(this.scheduledActivationMinutes.get(scheduleKey = koth.getName().toLowerCase()))) continue;
            this.scheduledActivationMinutes.put(scheduleKey, minuteKey);
            if (this.activeMatch == null || !this.activeMatch.isActive()) {
                this.startKoth(koth.getName());
                continue;
            }
            this.plugin.getLogger().warning("No se inicio el KOTH programado " + koth.getName() + " porque ya hay un KOTH activo.");
        }
    }

    private static final class CommandRewardMigration {
        private final String command;
        private Double chance;

        private CommandRewardMigration(String command, Double chance) {
            this.command = command;
            this.chance = chance;
        }
    }
}

