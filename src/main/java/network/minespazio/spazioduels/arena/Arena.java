/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 */
package network.minespazio.spazioduels.arena;

import network.minespazio.spazioduels.arena.ArenaRollback;
import network.minespazio.spazioduels.arena.ArenaState;
import org.bukkit.Location;

public class Arena {
    private final String name;
    private Location spawn1;
    private Location spawn2;
    private Location spectatorSpawn;
    private ArenaState state;
    private int maxDurationSeconds;
    private final ArenaRollback rollback;

    public Arena(String name) {
        this.name = name;
        this.state = ArenaState.IN_SETUP;
        this.maxDurationSeconds = 600;
        this.rollback = new ArenaRollback();
    }

    public Arena(String name, Location spawn1, Location spawn2, Location spectatorSpawn, ArenaState state, int maxDurationSeconds) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.spectatorSpawn = spectatorSpawn;
        this.state = state != null ? state : ArenaState.IN_SETUP;
        this.maxDurationSeconds = maxDurationSeconds > 0 ? maxDurationSeconds : 600;
        this.rollback = new ArenaRollback();
    }

    public boolean isReady() {
        return this.spawn1 != null && this.spawn2 != null;
    }

    public String getName() {
        return this.name;
    }

    public Location getSpawn1() {
        return this.spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
        this.checkStatus();
    }

    public Location getSpawn2() {
        return this.spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
        this.checkStatus();
    }

    public Location getSpectatorSpawn() {
        return this.spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public ArenaState getState() {
        return this.state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public int getMaxDurationSeconds() {
        return this.maxDurationSeconds;
    }

    public void setMaxDurationSeconds(int maxDurationSeconds) {
        this.maxDurationSeconds = maxDurationSeconds;
    }

    public ArenaRollback getRollback() {
        return this.rollback;
    }

    public void checkStatus() {
        if (this.state == ArenaState.IN_SETUP && this.isReady()) {
            this.state = ArenaState.AVAILABLE;
        }
    }
}

