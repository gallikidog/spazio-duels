package network.minespazio.spazioduels.koth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;

public class KothMatch {

    private final SpazioDuelsPlugin plugin;
    private final Koth koth;
    private final Set<UUID> playersInOuterZone = new HashSet<>();
    private Player currentCapper;
    private int remainingSeconds;
    private boolean active;
    private BukkitTask task;

    public KothMatch(SpazioDuelsPlugin plugin, Koth koth) {
        this.plugin = plugin;
        this.koth = koth;
        this.remainingSeconds = koth.getCaptureDelaySeconds();
        this.active = false;
    }

    public void start() {
        this.active = true;
        this.remainingSeconds = koth.getCaptureDelaySeconds();
        Bukkit.broadcast(TextUtil.toComponent("&e-------------------------------------------"));
        Bukkit.broadcast(TextUtil.toComponent("              &6&lKOTH INICIADO"));
        Bukkit.broadcast(TextUtil.toComponent("	 &fEl KOTH &b" + koth.getName() + " &fha comenzado!"));
        Bukkit.broadcast(TextUtil.toComponent("	 &7Tiempo de captura: &e" + formatTime(remainingSeconds)));
        Bukkit.broadcast(TextUtil.toComponent("&e-------------------------------------------"));

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }
                tick();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ticks every 1 second
    }

    private void tick() {
        // 1. Check outer zone entry for screen title (2 seconds duration)
        CuboidRegion outerZone = koth.getZone() != null ? koth.getZone() : koth.getCapZone();
        if (outerZone != null) {
            Set<UUID> currentInZone = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (outerZone.contains(player.getLocation())) {
                    currentInZone.add(player.getUniqueId());
                    if (!playersInOuterZone.contains(player.getUniqueId())) {
                        // Display entry title for 2 seconds
                        sendEntryTitle(player);
                    }
                }
            }
            playersInOuterZone.clear();
            playersInOuterZone.addAll(currentInZone);
        }

        // 2. Check inner capture zone
        CuboidRegion capZone = koth.getCapZone();
        if (capZone == null) return;

        List<Player> capperCandidates = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOnline() && !p.isDead() && capZone.contains(p.getLocation())) {
                capperCandidates.add(p);
            }
        }

        if (currentCapper != null) {
            // Verify if current capper is still inside cap zone
            if (!currentCapper.isOnline() || currentCapper.isDead() || !capZone.contains(currentCapper.getLocation())) {
                Bukkit.broadcast(TextUtil.toComponent("&c[KOTH] &e" + currentCapper.getName() + " &7ha perdido el control del KOTH &b" + koth.getName() + "&7."));
                currentCapper = null;
                remainingSeconds = koth.getCaptureDelaySeconds();
            }
        }

        if (currentCapper == null && !capperCandidates.isEmpty()) {
            currentCapper = capperCandidates.get(0);
            remainingSeconds = koth.getCaptureDelaySeconds();
            Bukkit.broadcast(TextUtil.toComponent("&a[KOTH] &e" + currentCapper.getName() + " &7ha comenzado a capturar el KOTH &b" + koth.getName() + "&7!"));
        }

        if (currentCapper != null) {
            remainingSeconds--;

            // Actionbar update
            String actionbarText = TextUtil.colorize("&eCapturando KOTH &b" + koth.getName() + " &7| &a" + currentCapper.getName() + " &7(" + formatTime(remainingSeconds) + ")");
            currentCapper.sendActionBar(TextUtil.toComponent(actionbarText));

            if (remainingSeconds <= 0) {
                finishMatch(currentCapper);
            }
        }
    }

    private void sendEntryTitle(Player player) {
        String titleRaw = plugin.getConfig().getString("koth.zone_entry_title", "&6&lKOTH");
        String subRaw = plugin.getConfig().getString("koth.zone_entry_subtitle", "&fIngresaste a la zona del Koth &b%koth%");
        subRaw = subRaw.replace("%koth%", koth.getName());

        Component mainTitle = TextUtil.toComponent(titleRaw);
        Component subtitle = TextUtil.toComponent(subRaw);

        // 2 seconds display duration (FadeIn: 0.2s, Stay: 1.6s, FadeOut: 0.2s)
        Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1600), Duration.ofMillis(200))
        );
        player.showTitle(title);
    }

    private void finishMatch(Player winner) {
        active = false;
        if (task != null) task.cancel();

        // 1. Send winner title for 2 seconds
        String winTitleRaw = plugin.getConfig().getString("koth.winner_title", "&a&l¡FELICITACIONES!");
        String winSubRaw = plugin.getConfig().getString("koth.winner_subtitle", "&fCapturaste el Koth &b%koth%");
        winSubRaw = winSubRaw.replace("%koth%", koth.getName());

        Title victoryTitle = Title.title(
                TextUtil.toComponent(winTitleRaw),
                TextUtil.toComponent(winSubRaw),
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1600), Duration.ofMillis(200))
        );
        winner.showTitle(victoryTitle);

        // 2. Broadcast winner message in server chat
        List<String> winBroadcast = plugin.getConfig().getStringList("koth.broadcast_win");
        if (winBroadcast == null || winBroadcast.isEmpty()) {
            winBroadcast = Arrays.asList(
                    "&e-------------------------------------------",
                    "              &6&lKOTH CAPTURADO",
                    "",
                    "	 &fEl jugador &b%player% &fha capturado el KOTH &e%koth%&f!",
                    "",
                    "&e-------------------------------------------"
            );
        }

        for (String line : winBroadcast) {
            String formatted = line.replace("%player%", winner.getName()).replace("%koth%", koth.getName());
            Bukkit.broadcast(TextUtil.toComponent(formatted));
        }

        // 3. Give loot to winner
        if (koth.getLootItems() != null && !koth.getLootItems().isEmpty()) {
            for (ItemStack loot : koth.getLootItems()) {
                if (loot != null && !loot.getType().isAir()) {
                    HashMap<Integer, ItemStack> leftover = winner.getInventory().addItem(loot.clone());
                    for (ItemStack item : leftover.values()) {
                        winner.getWorld().dropItemNaturally(winner.getLocation(), item);
                    }
                }
            }
            winner.sendMessage(TextUtil.colorize("&a¡Has recibido la recompensa del KOTH " + koth.getName() + "!"));
        }

        plugin.getKothManager().stopActiveMatch();
    }

    public void stopManually() {
        active = false;
        if (task != null) task.cancel();
        Bukkit.broadcast(TextUtil.toComponent("&c[KOTH] El KOTH &b" + koth.getName() + " &cha sido detenido por un administrador."));
    }

    public Koth getKoth() {
        return koth;
    }

    public Player getCurrentCapper() {
        return currentCapper;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public String formatTime(int totalSecs) {
        int minutes = totalSecs / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
