/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package network.minespazio.spazioduels.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import network.minespazio.spazioduels.SpazioDuelsPlugin;
import network.minespazio.spazioduels.arena.Arena;
import network.minespazio.spazioduels.arena.ArenaState;
import network.minespazio.spazioduels.duel.DuelMode;
import network.minespazio.spazioduels.duel.DuelTeam;
import network.minespazio.spazioduels.duel.HandicapRule;
import network.minespazio.spazioduels.kit.Kit;
import network.minespazio.spazioduels.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    private final Map<UUID, Location> preDuelLocations = new HashMap<UUID, Location>();
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
        this.remainingSeconds = arena.getMaxDurationSeconds() > 0 ? arena.getMaxDurationSeconds() : 600;
        this.arena.setState(ArenaState.BUSY);
    }

    public void startMatchSequence() {
        this.countingDown = true;
        this.setupTeam(this.team1, this.arena.getSpawn1());
        this.setupTeam(this.team2, this.arena.getSpawn2());
        if (this.mode.isHandicap() && this.handicapRule != null) {
            DuelTeam majority = this.team1.getMembers().size() > this.team2.getMembers().size() ? this.team1 : this.team2;
            for (Player p : majority.getOnlinePlayers()) {
                this.handicapRule.applyHandicap(p);
            }
        }
        new BukkitRunnable(){
            int countdown = 5;

            public void run() {
                if (DuelMatch.this.finished) {
                    this.cancel();
                    return;
                }
                if (this.countdown > 0) {
                    DuelMatch.this.broadcastMessage("&eEl duelo inicia en &c" + this.countdown + " &esegundos...");
                    DuelMatch.this.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    --this.countdown;
                } else {
                    this.cancel();
                    DuelMatch.this.countingDown = false;
                    DuelMatch.this.started = true;
                    DuelMatch.this.startTime = System.currentTimeMillis();
                    DuelMatch.this.broadcastMessage("&a&l\u00a1EL DUELO HA COMENZADO!");
                    DuelMatch.this.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                    DuelMatch.this.startMatchTimer();
                }
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    private void setupTeam(DuelTeam team, Location spawn) {
        for (Player player : team.getOnlinePlayers()) {
            this.preDuelLocations.put(player.getUniqueId(), player.getLocation().clone());
            this.plugin.getInventoryBackupManager().saveBackup(player);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.setInvulnerable(false);
            player.setNoDamageTicks(0);
            player.setFallDistance(0.0f);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            if (this.plugin.getPvP18Manager() != null) {
                this.plugin.getPvP18Manager().enable18PvP(player);
            }
            if (spawn != null) {
                player.teleport(spawn);
            }
            if (this.kit != null) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                boolean pkGiven = false;
                if (this.kit.isFromPlayerKits() && this.plugin.getKitManager().getPlayerKitsHook().isEnabled()) {
                    pkGiven = this.plugin.getKitManager().getPlayerKitsHook().giveKitToPlayer(player, this.kit.getName());
                }
                if (!pkGiven) {
                    ItemStack[] armor;
                    ItemStack[] fixed;
                    ItemStack[] contents = this.kit.getContents();
                    if (contents.length != 36) {
                        fixed = new ItemStack[36];
                        System.arraycopy(contents, 0, fixed, 0, Math.min(contents.length, 36));
                        contents = fixed;
                    }
                    if ((armor = this.kit.getArmor()).length != 4) {
                        fixed = new ItemStack[4];
                        System.arraycopy(armor, 0, fixed, 0, Math.min(armor.length, 4));
                        armor = fixed;
                    }
                    player.getInventory().setStorageContents(this.plugin.getAntiDupeManager().markKitItems(contents));
                    player.getInventory().setArmorContents(this.plugin.getAntiDupeManager().markKitItems(armor));
                    if (this.kit.getOffHand() != null) {
                        player.getInventory().setItemInOffHand(this.plugin.getAntiDupeManager().markKitItem(this.kit.getOffHand()));
                    } else {
                        player.getInventory().setItemInOffHand(null);
                    }
                    for (PotionEffect effect : this.kit.getPotionEffects()) {
                        player.addPotionEffect(effect);
                    }
                } else {
                    this.plugin.getKitManager().autoEquipArmor(player);
                    this.plugin.getKitManager().compactHotbar(player);
                    this.plugin.getAntiDupeManager().markPlayerInventory(player);
                }
            }
            player.updateInventory();
        }
    }

    private void startMatchTimer() {
        this.timerTask = new BukkitRunnable(){

            public void run() {
                if (DuelMatch.this.finished) {
                    this.cancel();
                    return;
                }
                if (DuelMatch.this.remainingSeconds <= 0) {
                    this.cancel();
                    DuelMatch.this.broadcastMessage("&c&l\u00a1EL TIEMPO MAXIMO DEL DUELO (10 MINUTOS) HA EXPIRADO!");
                    DuelMatch.this.handleTimeout();
                    return;
                }
                int min = DuelMatch.this.remainingSeconds / 60;
                int sec = DuelMatch.this.remainingSeconds % 60;
                String timeFormatted = String.format("%02d:%02d", min, sec);
                String actionbar = TextUtil.colorize("&eTiempo restante: &b" + timeFormatted + " &7| Arena: &a" + DuelMatch.this.arena.getName());
                DuelMatch.this.sendActionBar(actionbar);
                --DuelMatch.this.remainingSeconds;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    public void handlePlayerDeath(Player victim, Player killer) {
        if (this.finished) {
            return;
        }
        DuelTeam victimTeam = this.getTeamOf(victim);
        if (victimTeam == null) {
            return;
        }
        victimTeam.markDead(victim.getUniqueId());
        this.plugin.getAntiDupeManager().purgeKitItems(victim);
        victim.getInventory().clear();
        victim.getInventory().setArmorContents(null);
        this.broadcastMessage("&c" + victim.getName() + " &7ha sido eliminado del duelo.");
        if (victimTeam.isEliminated()) {
            DuelTeam winningTeam = victimTeam == this.team1 ? this.team2 : this.team1;
            this.endMatch(winningTeam);
        } else {
            victim.setGameMode(GameMode.SPECTATOR);
            if (this.arena.getSpectatorSpawn() != null) {
                victim.teleport(this.arena.getSpectatorSpawn());
            }
        }
    }

    public void handlePlayerQuit(Player player) {
        if (this.finished) {
            return;
        }
        DuelTeam team = this.getTeamOf(player);
        if (team != null) {
            this.broadcastMessage("&c" + player.getName() + " &7se ha desconectado durante el duelo.");
            team.markDead(player.getUniqueId());
            if (team.isEliminated()) {
                DuelTeam winningTeam = team == this.team1 ? this.team2 : this.team1;
                this.endMatch(winningTeam);
            }
        }
    }

    public void handleForfeit(Player player) {
        if (this.finished) {
            return;
        }
        DuelTeam team = this.getTeamOf(player);
        if (team != null) {
            this.broadcastMessage("&c" + player.getName() + " &7se ha rendido.");
            team.markDead(player.getUniqueId());
            if (team.isEliminated()) {
                DuelTeam winningTeam = team == this.team1 ? this.team2 : this.team1;
                this.endMatch(winningTeam);
            }
        }
    }

    private void handleTimeout() {
        if (this.team1.getOnlineAlivePlayers().size() > this.team2.getOnlineAlivePlayers().size()) {
            this.endMatch(this.team1);
        } else if (this.team2.getOnlineAlivePlayers().size() > this.team1.getOnlineAlivePlayers().size()) {
            this.endMatch(this.team2);
        } else if (this.team1.getTotalDamage() >= this.team2.getTotalDamage()) {
            this.endMatch(this.team1);
        } else {
            this.endMatch(this.team2);
        }
    }

    public void endMatch(DuelTeam winningTeam) {
        String hp;
        int hits;
        String name;
        Player p;
        if (this.finished) {
            return;
        }
        this.finished = true;
        this.endTime = System.currentTimeMillis();
        this.winner = winningTeam;
        if (this.timerTask != null) {
            this.timerTask.cancel();
        }
        this.broadcastMessage("&a&l=====================================");
        this.broadcastMessage("&e&l          DUELO FINALIZADO");
        this.broadcastMessage("&7Ganador: &a" + (winningTeam != null ? winningTeam.getFormattedMembers() : "Empate"));
        this.broadcastMessage("");
        this.broadcastMessage("&f&lEstad\u00edsticas de Jugadores:");
        this.broadcastMessage("&b&l" + this.team1.getName() + ":");
        for (UUID uuid : this.team1.getMembers()) {
            p = Bukkit.getPlayer((UUID)uuid);
            name = p != null ? p.getName() : "Desconocido";
            hits = this.team1.getHits(uuid);
            if (this.team1.getAliveMembers().contains(uuid) && p != null && p.isOnline() && !p.isDead()) {
                hp = String.format("%.1f", p.getHealth());
                this.broadcastMessage(" &7- &f" + name + ": &a" + hp + " \u2764 &7| &e" + hits + " golpes");
                continue;
            }
            this.broadcastMessage(" &7- &f" + name + ": &cMUERTO &7| &e" + hits + " golpes");
        }
        this.broadcastMessage("&c&l" + this.team2.getName() + ":");
        for (UUID uuid : this.team2.getMembers()) {
            p = Bukkit.getPlayer((UUID)uuid);
            name = p != null ? p.getName() : "Desconocido";
            hits = this.team2.getHits(uuid);
            if (this.team2.getAliveMembers().contains(uuid) && p != null && p.isOnline() && !p.isDead()) {
                hp = String.format("%.1f", p.getHealth());
                this.broadcastMessage(" &7- &f" + name + ": &a" + hp + " \u2764 &7| &e" + hits + " golpes");
                continue;
            }
            this.broadcastMessage(" &7- &f" + name + ": &cMUERTO &7| &e" + hits + " golpes");
        }
        this.broadcastMessage("&a&l=====================================");
        new BukkitRunnable(){

            public void run() {
                DuelMatch.this.cleanupAndRestore();
            }
        }.runTaskLater((Plugin)this.plugin, 60L);
    }

    public void cleanupAndRestore() {
        List<Player> allPlayers = this.getAllPlayers();
        for (Player p : allPlayers) {
            if (p == null || !p.isOnline()) continue;
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFireTicks(0);
            p.setFallDistance(0.0f);
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            if (this.plugin.getPvP18Manager() != null) {
                this.plugin.getPvP18Manager().restoreVanillaPvP(p);
            }
            this.plugin.getAntiDupeManager().purgeKitItems(p);
            this.plugin.getInventoryBackupManager().restoreBackup(p);
            Location targetLoc = this.plugin.getArenaManager().getGlobalLobbySpawn();
            if (targetLoc == null || targetLoc.getWorld() == null) {
                targetLoc = this.preDuelLocations.get(p.getUniqueId());
            }
            if (targetLoc == null || targetLoc.getWorld() == null) {
                targetLoc = this.plugin.getInventoryBackupManager().getSavedLocation(p);
            }
            if (targetLoc == null || targetLoc.getWorld() == null) {
                World mainWorld = Bukkit.getWorlds().isEmpty() ? null : (World)Bukkit.getWorlds().get(0);
                World world = mainWorld;
                if (mainWorld != null) {
                    targetLoc = mainWorld.getSpawnLocation();
                }
            }
            if (targetLoc != null) {
                p.teleport(targetLoc);
            }
            this.plugin.getDuelEventManager().deliverPendingRewards(p);
        }
        this.arena.getRollback().rollback();
        this.arena.setState(ArenaState.AVAILABLE);
        this.plugin.getDuelManager().unregisterMatch(this);
    }

    public DuelTeam getTeamOf(Player player) {
        if (player == null) {
            return null;
        }
        if (this.team1.getMembers().contains(player.getUniqueId())) {
            return this.team1;
        }
        if (this.team2.getMembers().contains(player.getUniqueId())) {
            return this.team2;
        }
        return null;
    }

    public List<Player> getAllPlayers() {
        ArrayList<Player> list = new ArrayList<Player>();
        list.addAll(this.team1.getOnlinePlayers());
        list.addAll(this.team2.getOnlinePlayers());
        return list;
    }

    public void broadcastMessage(String msg) {
        String colored = TextUtil.colorize(msg);
        for (Player p : this.getAllPlayers()) {
            p.sendMessage(colored);
        }
    }

    public void sendActionBar(String msg) {
        Component comp = TextUtil.toComponent(msg);
        for (Player p : this.getAllPlayers()) {
            p.sendActionBar(comp);
        }
    }

    public void playSound(Sound sound, float vol, float pitch) {
        for (Player p : this.getAllPlayers()) {
            p.playSound(p.getLocation(), sound, vol, pitch);
        }
    }

    public UUID getMatchId() {
        return this.matchId;
    }

    public Arena getArena() {
        return this.arena;
    }

    public Kit getKit() {
        return this.kit;
    }

    public DuelMode getMode() {
        return this.mode;
    }

    public DuelTeam getTeam1() {
        return this.team1;
    }

    public DuelTeam getTeam2() {
        return this.team2;
    }

    public DuelTeam getWinner() {
        return this.winner;
    }

    public boolean isCountingDown() {
        return this.countingDown;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public long getDurationSeconds() {
        if (this.startTime == 0L) {
            return 0L;
        }
        long end = this.endTime > 0L ? this.endTime : System.currentTimeMillis();
        return (end - this.startTime) / 1000L;
    }
}

