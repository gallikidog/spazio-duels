/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.title.Title
 *  net.kyori.adventure.title.Title$Times
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package network.minespazio.spazioduels.koth;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.Koth;
import network.minespazio.spazioduels.koth.KothCaptureSnapshot;
import network.minespazio.spazioduels.koth.KothCommandReward;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class KothMatch {
    private final SpazioDuelsPlugin plugin;
    private final Koth koth;
    private final Set<UUID> playersInOuterZone = new HashSet<UUID>();
    private UUID currentCapperId;
    private int remainingSeconds;
    private long captureEndTimestampMillis;
    private long eventEndTimestampMillis;
    private boolean active;
    private BukkitTask task;
    private KothCaptureSnapshot captureSnapshot = KothCaptureSnapshot.inactive();

    public KothMatch(SpazioDuelsPlugin plugin, Koth koth) {
        this.plugin = plugin;
        this.koth = koth;
        this.remainingSeconds = koth.getCaptureDelaySeconds();
        this.active = false;
    }

    public void start() {
        this.active = true;
        this.remainingSeconds = this.koth.getCaptureDelaySeconds();
        int maxEventSeconds = Math.max(1, this.plugin.getConfig().getInt("koth.max_event_duration_seconds", 300));
        this.eventEndTimestampMillis = System.currentTimeMillis() + (long)maxEventSeconds * 1000L;
        this.refreshCaptureSnapshot();
        Bukkit.broadcast((Component)TextUtil.toComponent("&e-------------------------------------------"));
        Bukkit.broadcast((Component)TextUtil.toComponent("              &6&lKOTH INICIADO"));
        Bukkit.broadcast((Component)TextUtil.toComponent("\t &fEl KOTH &b" + this.koth.getName() + " &fha comenzado!"));
        Bukkit.broadcast((Component)TextUtil.toComponent("\t &7Tiempo de captura: &e" + this.formatTime(this.remainingSeconds)));
        Bukkit.broadcast((Component)TextUtil.toComponent("&e-------------------------------------------"));
        this.task = new BukkitRunnable(){

            public void run() {
                if (!KothMatch.this.active) {
                    this.cancel();
                    return;
                }
                KothMatch.this.tick();
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void tick() {
        if (System.currentTimeMillis() >= this.eventEndTimestampMillis) {
            this.finishDueToTimeout();
            return;
        }
        CuboidRegion outerZone = this.koth.getZone() != null ? this.koth.getZone() : this.koth.getCapZone();
        CuboidRegion capZone = this.koth.getCapZone();
        HashSet<UUID> currentInZone = outerZone == null ? null : new HashSet<UUID>();
        Player capperCandidate = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();
            UUID playerId = player.getUniqueId();
            if (outerZone != null && outerZone.contains(location)) {
                currentInZone.add(playerId);
                if (!this.playersInOuterZone.contains(playerId)) {
                    this.sendEntryTitle(player);
                }
            }
            if (capperCandidate != null || capZone == null || player.isDead() || !capZone.contains(location)) continue;
            capperCandidate = player;
        }
        if (currentInZone != null) {
            this.playersInOuterZone.clear();
            this.playersInOuterZone.addAll(currentInZone);
        }
        if (capZone == null) {
            this.refreshCaptureSnapshot();
            return;
        }
        Player currentCapper = this.getCurrentCapper();
        if (!(currentCapper == null || currentCapper.isOnline() && !currentCapper.isDead() && capZone.contains(currentCapper.getLocation()))) {
            Bukkit.broadcast((Component)TextUtil.toComponent("&c[KOTH] &e" + (currentCapper == null ? "El jugador" : currentCapper.getName()) + " &7ha perdido el control del KOTH &b" + this.koth.getName() + "&7."));
            this.currentCapperId = null;
            this.remainingSeconds = this.koth.getCaptureDelaySeconds();
            this.captureEndTimestampMillis = 0L;
        }
        if (this.currentCapperId == null && capperCandidate != null) {
            this.currentCapperId = capperCandidate.getUniqueId();
            this.remainingSeconds = this.koth.getCaptureDelaySeconds();
            this.captureEndTimestampMillis = System.currentTimeMillis() + (long)this.remainingSeconds * 1000L;
            Bukkit.broadcast((Component)TextUtil.toComponent("&a[KOTH] &e" + capperCandidate.getName() + " &7ha comenzado a capturar el KOTH &b" + this.koth.getName() + "&7!"));
        }
        if ((currentCapper = this.getCurrentCapper()) != null) {
            --this.remainingSeconds;
            String actionbarText = TextUtil.colorize("&eCapturando KOTH &b" + this.koth.getName() + " &7| &a" + currentCapper.getName() + " &7(" + this.formatTime(this.remainingSeconds) + ")");
            currentCapper.sendActionBar(TextUtil.toComponent(actionbarText));
            if (this.remainingSeconds <= 0) {
                this.finishMatch(currentCapper);
                return;
            }
        }
        this.refreshCaptureSnapshot();
    }

    private void sendEntryTitle(Player player) {
        String titleRaw = this.plugin.getConfig().getString("koth.zone_entry_title", "&6&lKOTH");
        String subRaw = this.plugin.getConfig().getString("koth.zone_entry_subtitle", "&fIngresaste a la zona del Koth &b%koth%");
        subRaw = subRaw.replace("%koth%", this.koth.getName());
        Component mainTitle = TextUtil.toComponent(titleRaw);
        Component subtitle = TextUtil.toComponent(subRaw);
        Title title = Title.title((Component)mainTitle, (Component)subtitle, (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofMillis(1600L), (Duration)Duration.ofMillis(200L)));
        player.showTitle(title);
    }

    private void finishMatch(Player winner) {
        this.active = false;
        this.currentCapperId = null;
        this.playersInOuterZone.clear();
        this.refreshCaptureSnapshot();
        if (this.task != null) {
            this.task.cancel();
        }
        String winTitleRaw = this.plugin.getConfig().getString("koth.winner_title", "&a&l\u00a1FELICITACIONES!");
        String winSubRaw = this.plugin.getConfig().getString("koth.winner_subtitle", "&fCapturaste el Koth &b%koth%");
        winSubRaw = winSubRaw.replace("%koth%", this.koth.getName());
        Title victoryTitle = Title.title((Component)TextUtil.toComponent(winTitleRaw), (Component)TextUtil.toComponent(winSubRaw), (Title.Times)Title.Times.times((Duration)Duration.ofMillis(200L), (Duration)Duration.ofMillis(1600L), (Duration)Duration.ofMillis(200L)));
        winner.showTitle(victoryTitle);
        List<String> winBroadcast = this.plugin.getConfig().getStringList("koth.broadcast_win");
        if (winBroadcast == null || winBroadcast.isEmpty()) {
            winBroadcast = Arrays.asList("&e-------------------------------------------", "              &6&lKOTH CAPTURADO", "", "\t &fEl jugador &b%player% &fha capturado el KOTH &e%koth%&f!", "", "&e-------------------------------------------");
        }
        for (String line : winBroadcast) {
            String formatted = line.replace("%player%", winner.getName()).replace("%koth%", this.koth.getName());
            Bukkit.broadcast((Component)TextUtil.toComponent(formatted));
        }
        if (this.koth.getLootItems() != null && !this.koth.getLootItems().isEmpty()) {
            for (ItemStack loot : this.koth.getLootItems()) {
                if (loot == null || loot.getType().isAir()) continue;
                HashMap<Integer, ItemStack> leftover = winner.getInventory().addItem(new ItemStack[]{loot.clone()});
                for (ItemStack item : leftover.values()) {
                    winner.getWorld().dropItemNaturally(winner.getLocation(), item);
                }
            }
            winner.sendMessage(TextUtil.colorize("&a\u00a1Has recibido la recompensa del KOTH " + this.koth.getName() + "!"));
        }
        this.executeCommandRewards(winner);
        this.plugin.getKothManager().stopActiveMatch();
    }

    private void finishDueToTimeout() {
        this.active = false;
        this.currentCapperId = null;
        this.playersInOuterZone.clear();
        this.refreshCaptureSnapshot();
        if (this.task != null) {
            this.task.cancel();
        }
        Bukkit.broadcast((Component)TextUtil.toComponent("&c[KOTH] El KOTH &b" + this.koth.getName() + " &cfinalizo porque alcanzo el limite de tiempo."));
        this.plugin.getKothManager().stopActiveMatch();
    }

    public void stopManually() {
        this.active = false;
        this.currentCapperId = null;
        this.playersInOuterZone.clear();
        this.refreshCaptureSnapshot();
        if (this.task != null) {
            this.task.cancel();
        }
        Bukkit.broadcast((Component)TextUtil.toComponent("&c[KOTH] El KOTH &b" + this.koth.getName() + " &cha sido detenido por un administrador."));
    }

    public Koth getKoth() {
        return this.koth;
    }

    public Player getCurrentCapper() {
        return this.currentCapperId == null ? null : Bukkit.getPlayer((UUID)this.currentCapperId);
    }

    public int getRemainingSeconds() {
        return this.remainingSeconds;
    }

    public boolean isActive() {
        return this.active;
    }

    public KothCaptureSnapshot getCaptureSnapshot() {
        return this.captureSnapshot;
    }

    public long getRemainingMillis() {
        if (this.currentCapperId == null || this.captureEndTimestampMillis <= 0L) {
            return (long)this.remainingSeconds * 1000L;
        }
        return Math.max(0L, this.captureEndTimestampMillis - System.currentTimeMillis());
    }

    public int getEventRemainingSeconds() {
        if (!this.active || this.eventEndTimestampMillis <= 0L) {
            return 0;
        }
        return (int)Math.max(0L, (this.eventEndTimestampMillis - System.currentTimeMillis()) / 1000L);
    }

    public String formatEventTimeLeft() {
        return this.formatTime(this.getEventRemainingSeconds());
    }

    public String formatTime(int totalSecs) {
        int minutes = totalSecs / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String formatTimeMillis() {
        long totalMillis = this.getRemainingMillis();
        long totalSecs = totalMillis / 1000L;
        long minutes = totalSecs / 60L;
        long seconds = totalSecs % 60L;
        long tenths = totalMillis % 1000L / 100L;
        return String.format("%02d:%02d.%d", minutes, seconds, tenths);
    }

    private void executeCommandRewards(Player winner) {
        for (KothCommandReward reward : this.koth.getCommandRewards()) {
            if (!reward.guaranteed()) continue;
            this.executeCommandReward(winner, reward.command());
        }
        double roll = ThreadLocalRandom.current().nextDouble(100.0);
        double accumulated = 0.0;
        for (KothCommandReward reward : this.koth.getCommandRewards()) {
            if (reward.guaranteed() || !(roll < (accumulated += reward.chance()))) continue;
            this.executeCommandReward(winner, reward.command());
            return;
        }
    }

    private void executeCommandReward(Player winner, String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        String resolved = command.replace("%player_name%", winner.getName()).replace("%player%", winner.getName()).replace("%player_uuid%", winner.getUniqueId().toString()).replace("%uuid%", winner.getUniqueId().toString()).replace("%koth%", this.koth.getName());
        try {
            if (!Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)resolved)) {
                this.plugin.getLogger().warning("No se encontro el comando de recompensa KOTH: " + resolved);
            }
        }
        catch (RuntimeException exception) {
            this.plugin.getLogger().warning("No se pudo ejecutar la recompensa KOTH '" + resolved + "': " + exception.getMessage());
        }
    }

    private void refreshCaptureSnapshot() {
        if (!this.active) {
            this.captureSnapshot = KothCaptureSnapshot.inactive();
            return;
        }
        Player capper = this.getCurrentCapper();
        boolean capturing = capper != null;
        int duration = Math.max(1, this.koth.getCaptureDelaySeconds());
        int remaining = Math.max(0, this.remainingSeconds);
        int progress = capturing ? Math.max(0, Math.min(100, (int)((long)(duration - remaining) * 100L / (long)duration))) : 0;
        int width = Math.max(1, Math.min(50, this.plugin.getConfig().getInt("koth.placeholders.progress_bar.width", 20)));
        String filled = this.getProgressSymbol("koth.placeholders.progress_bar.filled", "&a|");
        String empty = this.getProgressSymbol("koth.placeholders.progress_bar.empty", "&7|");
        int filledUnits = progress * width / 100;
        this.captureSnapshot = new KothCaptureSnapshot(true, this.koth.getName(), capturing ? capper.getName() : "Nadie", remaining, progress, filled.repeat(filledUnits) + empty.repeat(width - filledUnits), capturing ? "CAPTURANDO" : "ESPERANDO");
    }

    private String getProgressSymbol(String path, String fallback) {
        String symbol = this.plugin.getConfig().getString(path);
        return symbol == null || symbol.isEmpty() ? fallback : symbol;
    }
}

