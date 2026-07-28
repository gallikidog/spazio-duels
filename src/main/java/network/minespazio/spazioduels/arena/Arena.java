package network.minespazio.spazioduels.arena;

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
        this.maxDurationSeconds = 600; // 10 minutes default
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
        return spawn1 != null && spawn2 != null;
    }

    public String getName() {
        return name;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
        checkStatus();
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
        checkStatus();
    }

    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    public void setSpectatorSpawn(Location spectatorSpawn) {
        this.spectatorSpawn = spectatorSpawn;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public int getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    public void setMaxDurationSeconds(int maxDurationSeconds) {
        this.maxDurationSeconds = maxDurationSeconds;
    }

    public ArenaRollback getRollback() {
        return rollback;
    }

    public void checkStatus() {
        if (state == ArenaState.IN_SETUP && isReady()) {
            state = ArenaState.AVAILABLE;
        }
    }
}
