/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.configuration.ConfigurationSection
 */
package network.minespazio.spazioduels.koth;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class CuboidRegion {
    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public CuboidRegion(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }
        if (!loc.getWorld().getName().equals(this.worldName)) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getMinX() {
        return this.minX;
    }

    public int getMinY() {
        return this.minY;
    }

    public int getMinZ() {
        return this.minZ;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public int getMaxZ() {
        return this.maxZ;
    }

    public void toConfig(ConfigurationSection sec) {
        if (sec == null) {
            return;
        }
        sec.set("world", (Object)this.worldName);
        sec.set("min_x", (Object)this.minX);
        sec.set("min_y", (Object)this.minY);
        sec.set("min_z", (Object)this.minZ);
        sec.set("max_x", (Object)this.maxX);
        sec.set("max_y", (Object)this.maxY);
        sec.set("max_z", (Object)this.maxZ);
    }

    public static CuboidRegion fromConfig(ConfigurationSection sec) {
        if (sec == null || !sec.contains("world")) {
            return null;
        }
        String world = sec.getString("world");
        int minX = sec.getInt("min_x");
        int minY = sec.getInt("min_y");
        int minZ = sec.getInt("min_z");
        int maxX = sec.getInt("max_x");
        int maxY = sec.getInt("max_y");
        int maxZ = sec.getInt("max_z");
        return new CuboidRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
}

