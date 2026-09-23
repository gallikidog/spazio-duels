/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 */
package network.minespazio.spazioduels.koth;

import java.util.ArrayList;
import java.util.List;
import network.minespazio.spazioduels.koth.CuboidRegion;
import network.minespazio.spazioduels.koth.KothCommandReward;
import network.minespazio.spazioduels.koth.KothSchedule;
import org.bukkit.inventory.ItemStack;

public class Koth {
    public static final int MAX_COMMAND_REWARDS = 3;
    private final String name;
    private int captureDelaySeconds;
    private CuboidRegion zone;
    private CuboidRegion capZone;
    private List<ItemStack> lootItems;
    private List<KothCommandReward> commandRewards;
    private List<String> activationTimes;

    public Koth(String name) {
        this.name = name;
        this.captureDelaySeconds = 300;
        this.zone = null;
        this.capZone = null;
        this.lootItems = new ArrayList<ItemStack>();
        this.commandRewards = new ArrayList<KothCommandReward>();
        this.activationTimes = new ArrayList<String>();
    }

    public Koth(String name, int captureDelaySeconds, CuboidRegion zone, CuboidRegion capZone, List<ItemStack> lootItems, List<KothCommandReward> commandRewards, List<String> activationTimes) {
        this.name = name;
        this.captureDelaySeconds = captureDelaySeconds > 0 ? captureDelaySeconds : 300;
        this.zone = zone;
        this.capZone = capZone;
        this.lootItems = lootItems != null ? lootItems : new ArrayList();
        this.commandRewards = new ArrayList<KothCommandReward>();
        this.activationTimes = new ArrayList<String>();
        this.setCommandRewards(commandRewards);
        this.setActivationTimes(activationTimes);
    }

    public String getName() {
        return this.name;
    }

    public int getCaptureDelaySeconds() {
        return this.captureDelaySeconds;
    }

    public void setCaptureDelaySeconds(int captureDelaySeconds) {
        this.captureDelaySeconds = captureDelaySeconds;
    }

    public CuboidRegion getZone() {
        return this.zone;
    }

    public void setZone(CuboidRegion zone) {
        this.zone = zone;
    }

    public CuboidRegion getCapZone() {
        return this.capZone;
    }

    public void setCapZone(CuboidRegion capZone) {
        this.capZone = capZone;
    }

    public List<ItemStack> getLootItems() {
        return this.lootItems;
    }

    public void setLootItems(List<ItemStack> lootItems) {
        this.lootItems = lootItems != null ? lootItems : new ArrayList();
    }

    public List<KothCommandReward> getCommandRewards() {
        return this.commandRewards;
    }

    public void setCommandRewards(List<KothCommandReward> commandRewards) {
        this.commandRewards.clear();
        if (commandRewards == null) {
            return;
        }
        for (KothCommandReward reward : commandRewards) {
            if (reward == null) continue;
            if (reward.guaranteed()) {
                this.addGuaranteedCommandReward(reward.command());
                continue;
            }
            this.addCommandReward(reward.command(), reward.chance());
        }
    }

    public boolean addGuaranteedCommandReward(String command) {
        String normalized = Koth.normalizeCommand(command);
        if (normalized.isEmpty() || this.commandRewards.size() >= 3) {
            return false;
        }
        this.commandRewards.add(new KothCommandReward(normalized, 100.0, true));
        return true;
    }

    public boolean addCommandReward(String command, double chance) {
        String normalized = Koth.normalizeCommand(command);
        if (normalized.isEmpty() || this.commandRewards.size() >= 3 || chance <= 0.0 || chance > 100.0 || this.getWeightedChanceTotal() + chance > 100.000001) {
            return false;
        }
        this.commandRewards.add(new KothCommandReward(normalized, chance, false));
        return true;
    }

    public boolean removeCommandReward(int index) {
        if (index < 0 || index >= this.commandRewards.size()) {
            return false;
        }
        this.commandRewards.remove(index);
        return true;
    }

    public double getWeightedChanceTotal() {
        double total = 0.0;
        for (KothCommandReward reward : this.commandRewards) {
            if (reward.guaranteed()) continue;
            total += reward.chance();
        }
        return total;
    }

    private static String normalizeCommand(String command) {
        if (command == null) {
            return "";
        }
        String normalized = command.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1).trim();
        }
        return normalized;
    }

    public List<String> getActivationTimes() {
        return this.activationTimes;
    }

    public void setActivationTimes(List<String> activationTimes) {
        this.activationTimes.clear();
        if (activationTimes == null) {
            return;
        }
        for (String activationTime : activationTimes) {
            this.addActivationTime(activationTime);
        }
    }

    public boolean addActivationTime(String activationTime) {
        String normalized = KothSchedule.normalizeTime(activationTime);
        if (normalized == null || this.activationTimes.contains(normalized)) {
            return false;
        }
        this.activationTimes.add(normalized);
        this.activationTimes.sort(String::compareTo);
        return true;
    }

    public boolean isReady() {
        return this.capZone != null;
    }
}

