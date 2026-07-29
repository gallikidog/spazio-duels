package network.minespazio.spazioduels.duel;

import net.kyori.adventure.text.Component;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.arena.ArenaState;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DuelMatch {

    private final UUID matchId;
    private final SpazioDuelsPlugin plugin;
    private final Arena arena;
    private final Kit kit;
    private final DuelMode mode;
    private final DuelTeam team1;
    private final DuelTeam team2;
    private final HandicapRule handicapRule;

    private boolean countingDown;
    private boolean started;
    private boolean finished;
    private int remainingSeconds;
    private BukkitTask timerTask;
    private long startTime;
    private long endTime;

    private final Map<UUID, org.bukkit.Location> preDuelLocations = new HashMap<>();
    private DuelTeam winner;

    public DuelMatch(SpazioDuelsPlugin plugin, Arena arena, Kit kit, DuelMode mode, DuelTeam team1, DuelTeam team2, HandicapRule handicapRule) {
        this.matchId = UUID.randomUUID();
        this.plugin = plugin;
        this.arena = arena;
        this.kit = kit;
        this.mode = mode;
        this.team1 = team1;
        this.team2 = team2;
        this.handicapRule = handicapRule;
        this.remainingSeconds = arena.getMaxDurationSeconds() > 0 ? arena.getMaxDurationSeconds() : 600; // Default 10 min
        this.arena.setState(ArenaState.BUSY);
    }

    public void startMatchSequence() {
        countingDown = true;

        // Backup inventories & setup players
        setupTeam(team1, arena.getSpawn1());
        setupTeam(team2, arena.getSpawn2());

        // Apply handicap if applicable
        if (mode.isHandicap() && handicapRule != null) {
            // Apply handicap to majority team (team with more members)
            DuelTeam majority = team1.getMembers().size() > team2.getMembers().size() ? team1 : team2;
            for (Player p : majority.getOnlinePlayers()) {
                handicapRule.applyHandicap(p);
            }
        }

        // 5 second Countdown
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (finished) {
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    broadcastMessage("&eEl duelo inicia en &c" + countdown + " &esegundos...");
                    playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    countdown--;
                } else {
                    cancel();
                    countingDown = false;
                    started = true;
                    startTime = System.currentTimeMillis();
                    broadcastMessage("&a&l¡EL DUELO HA COMENZADO!");
                    playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                    startMatchTimer();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void setupTeam(DuelTeam team, org.bukkit.Location spawn) {
        for (Player player : team.getOnlinePlayers()) {
            // Save pre-duel location & inventory
            preDuelLocations.put(player.getUniqueId(), player.getLocation().clone());
            plugin.getInventoryBackupManager().saveBackup(player);

            // Clean inventory & potion effects
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);

            // Enable 1.8.9 PvP attack speed if configured
            if (plugin.getPvP18Manager() != null) {
                plugin.getPvP18Manager().enable18PvP(player);
            }

            // Teleport to arena spawn
            if (spawn != null) {
                player.teleport(spawn);
            }

            // Apply kit items with PDC anti-dupe tags
            if (kit != null) {
                player.getInventory().setStorageContents(plugin.getAntiDupeManager().markKitItems(kit.getContents()));
                player.getInventory().setArmorContents(plugin.getAntiDupeManager().markKitItems(kit.getArmor()));
                if (kit.getOffHand() != null) {
                    player.getInventory().setItemInOffHand(plugin.getAntiDupeManager().markKitItem(kit.getOffHand()));
                }
                for (PotionEffect effect : kit.getPotionEffects()) {
                    player.addPotionEffect(effect);
                }
            }
            player.updateInventory();
        }
    }

    private void startMatchTimer() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished) {
                    cancel();
                    return;
                }

                if (remainingSeconds <= 0) {
                    cancel();
                    broadcastMessage("&c&l¡EL TIEMPO MAXIMO DEL DUELO (10 MINUTOS) HA EXPIRADO!");
                    handleTimeout();
                    return;
                }

                // Actionbar timer update
                int min = remainingSeconds / 60;
                int sec = remainingSeconds % 60;
                String timeFormatted = String.format("%02d:%02d", min, sec);
                String actionbar = TextUtil.colorize("&eTiempo restante: &b" + timeFormatted + " &7| Arena: &a" + arena.getName());
                
                sendActionBar(actionbar);
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void handlePlayerDeath(Player victim, Player killer) {
        if (finished) return;

        DuelTeam victimTeam = getTeamOf(victim);
        if (victimTeam == null) return;

        victimTeam.markDead(victim.getUniqueId());

        // Clean kit items on death
        plugin.getAntiDupeManager().purgeKitItems(victim);
        victim.getInventory().clear();
        victim.getInventory().setArmorContents(null);

        broadcastMessage("&c" + victim.getName() + " &7ha sido eliminado del duelo.");

        // Check if victim team is eliminated
        if (victimTeam.isEliminated()) {
            DuelTeam winningTeam = (victimTeam == team1) ? team2 : team1;
            endMatch(winningTeam);
        } else {
            // Put victim in spectator mode until match ends
            victim.setGameMode(GameMode.SPECTATOR);
            if (arena.getSpectatorSpawn() != null) {
                victim.teleport(arena.getSpectatorSpawn());
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        if (finished) return;
        DuelTeam team = getTeamOf(player);
        if (team != null) {
            broadcastMessage("&c" + player.getName() + " &7se ha desconectado durante el duelo.");
            team.markDead(player.getUniqueId());

            if (team.isEliminated()) {
                DuelTeam winningTeam = (team == team1) ? team2 : team1;
                endMatch(winningTeam);
            }
        }
    }

    public void handleForfeit(Player player) {
        if (finished) return;
        DuelTeam team = getTeamOf(player);
        if (team != null) {
            broadcastMessage("&c" + player.getName() + " &7se ha rendido.");
            team.markDead(player.getUniqueId());

            if (team.isEliminated()) {
                DuelTeam winningTeam = (team == team1) ? team2 : team1;
                endMatch(winningTeam);
            }
        }
    }

    private void handleTimeout() {
        // Decide winner by higher remaining alive players or higher total damage
        if (team1.getOnlineAlivePlayers().size() > team2.getOnlineAlivePlayers().size()) {
            endMatch(team1);
        } else if (team2.getOnlineAlivePlayers().size() > team1.getOnlineAlivePlayers().size()) {
            endMatch(team2);
        } else if (team1.getTotalDamage() >= team2.getTotalDamage()) {
            endMatch(team1);
        } else {
            endMatch(team2);
        }
    }

    public void endMatch(DuelTeam winningTeam) {
        if (finished) return;
        finished = true;
        endTime = System.currentTimeMillis();
        this.winner = winningTeam;

        if (timerTask != null) {
            timerTask.cancel();
        }

        broadcastMessage("&a&l=====================================");
        broadcastMessage("&e&l          DUELO FINALIZADO");
        broadcastMessage("&7Ganador: &a" + (winningTeam != null ? winningTeam.getFormattedMembers() : "Empate"));
        broadcastMessage("");
        broadcastMessage("&f&lEstadísticas de Jugadores:");

        broadcastMessage("&b&l" + team1.getName() + ":");
        for (UUID uuid : team1.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            String name = p != null ? p.getName() : "Desconocido";
            int hits = team1.getHits(uuid);
            if (team1.getAliveMembers().contains(uuid) && p != null && p.isOnline() && !p.isDead()) {
                String hp = String.format("%.1f", p.getHealth());
                broadcastMessage(" &7- &f" + name + ": &a" + hp + " ❤ &7| &e" + hits + " golpes");
            } else {
                broadcastMessage(" &7- &f" + name + ": &cMUERTO &7| &e" + hits + " golpes");
            }
        }

        broadcastMessage("&c&l" + team2.getName() + ":");
        for (UUID uuid : team2.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            String name = p != null ? p.getName() : "Desconocido";
            int hits = team2.getHits(uuid);
            if (team2.getAliveMembers().contains(uuid) && p != null && p.isOnline() && !p.isDead()) {
                String hp = String.format("%.1f", p.getHealth());
                broadcastMessage(" &7- &f" + name + ": &a" + hp + " ❤ &7| &e" + hits + " golpes");
            } else {
                broadcastMessage(" &7- &f" + name + ": &cMUERTO &7| &e" + hits + " golpes");
            }
        }
        broadcastMessage("&a&l=====================================");

        // Teleport back and restore original inventories after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupAndRestore();
            }
        }.runTaskLater(plugin, 60L);
    }

    public void cleanupAndRestore() {
        List<Player> allPlayers = getAllPlayers();
        for (Player p : allPlayers) {
            if (p == null || !p.isOnline()) continue;

            // Reset player state (GameMode, Health, Potion effects, Fire, Fall distance)
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.setFallDistance(0.0f);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }

            // Restore vanilla 1.9+ attack speed if changed
            if (plugin.getPvP18Manager() != null) {
                plugin.getPvP18Manager().restoreVanillaPvP(p);
            }

            // Purge any remaining kit items
            plugin.getAntiDupeManager().purgeKitItems(p);

            // Restore original inventory from disk
            plugin.getInventoryBackupManager().restoreBackup(p);

            // Teleport to pre-duel location, lobby spawn, spectator spawn, or world spawn
            org.bukkit.Location targetLoc = preDuelLocations.get(p.getUniqueId());
            if (targetLoc == null || targetLoc.getWorld() == null) {
                targetLoc = plugin.getArenaManager().getGlobalLobbySpawn();
            }
            if (targetLoc == null || targetLoc.getWorld() == null) {
                targetLoc = arena.getSpectatorSpawn();
            }
            if (targetLoc == null || targetLoc.getWorld() == null) {
                targetLoc = p.getWorld().getSpawnLocation();
            }

            if (targetLoc != null) {
                p.teleport(targetLoc);
            }
        }

        // Rollback placed blocks in arena
        arena.getRollback().rollback();
        arena.setState(ArenaState.AVAILABLE);

        // Remove from DuelManager
        plugin.getDuelManager().unregisterMatch(this);
    }

    public DuelTeam getTeamOf(Player player) {
        if (player == null) return null;
        if (team1.getMembers().contains(player.getUniqueId())) return team1;
        if (team2.getMembers().contains(player.getUniqueId())) return team2;
        return null;
    }

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>();
        list.addAll(team1.getOnlinePlayers());
        list.addAll(team2.getOnlinePlayers());
        return list;
    }

    public void broadcastMessage(String msg) {
        String colored = TextUtil.colorize(msg);
        for (Player p : getAllPlayers()) {
            p.sendMessage(colored);
        }
    }

    public void sendActionBar(String msg) {
        Component comp = TextUtil.toComponent(msg);
        for (Player p : getAllPlayers()) {
            p.sendActionBar(comp);
        }
    }

    public void playSound(Sound sound, float vol, float pitch) {
        for (Player p : getAllPlayers()) {
            p.playSound(p.getLocation(), sound, vol, pitch);
        }
    }

    public UUID getMatchId() {
        return matchId;
    }

    public Arena getArena() {
        return arena;
    }

    public Kit getKit() {
        return kit;
    }

    public DuelMode getMode() {
        return mode;
    }

    public DuelTeam getTeam1() {
        return team1;
    }

    public DuelTeam getTeam2() {
        return team2;
    }

    public DuelTeam getWinner() {
        return winner;
    }

    public boolean isCountingDown() {
        return countingDown;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished;
    }

    public long getDurationSeconds() {
        if (startTime == 0) return 0;
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return (end - startTime) / 1000;
    }
}
