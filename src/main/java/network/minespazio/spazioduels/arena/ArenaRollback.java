/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockState
 */
package network.minespazio.spazioduels.arena;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class ArenaRollback {
    private final List<BlockState> placedBlockStates = new ArrayList<BlockState>();

    public void trackBlockPlace(Block block) {
        this.placedBlockStates.add(block.getState());
    }

    public void rollback() {
        for (int i = this.placedBlockStates.size() - 1; i >= 0; --i) {
            BlockState state = this.placedBlockStates.get(i);
            state.update(true, false);
        }
        this.placedBlockStates.clear();
    }

    public void clear() {
        this.placedBlockStates.clear();
    }
}

