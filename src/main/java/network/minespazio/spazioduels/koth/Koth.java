package network.minespazio.spazioduels.koth;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Koth {

    private final String name;
    private int captureDelaySeconds;
    private CuboidRegion zone;
    private CuboidRegion capZone;
    private List<ItemStack> lootItems;

    public Koth(String name) {
        this.name = name;
        this.captureDelaySeconds = 300; // 5 minutes default
        this.zone = null;
        this.capZone = null;
        this.lootItems = new ArrayList<>();
    }

    public Koth(String name, int captureDelaySeconds, CuboidRegion zone, CuboidRegion capZone,
            List<ItemStack> lootItems) {
        this.name = name;
        this.captureDelaySeconds = captureDelaySeconds > 0 ? captureDelaySeconds : 300;
        this.zone = zone;
        this.capZone = capZone;
        this.lootItems = lootItems != null ? lootItems : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getCaptureDelaySeconds() {
        return captureDelaySeconds;
    }

    public void setCaptureDelaySeconds(int captureDelaySeconds) {
        this.captureDelaySeconds = captureDelaySeconds;
    }

    public CuboidRegion getZone() {
        return zone;
    }

    public void setZone(CuboidRegion zone) {
        this.zone = zone;
    }

    public CuboidRegion getCapZone() {
        return capZone;
    }

    public void setCapZone(CuboidRegion capZone) {
        this.capZone = capZone;
    }

    public List<ItemStack> getLootItems() {
        return lootItems;
    }

    public void setLootItems(List<ItemStack> lootItems) {
        this.lootItems = lootItems != null ? lootItems : new ArrayList<>();
    }

    public boolean isReady() {
        return capZone != null;
    }
}
