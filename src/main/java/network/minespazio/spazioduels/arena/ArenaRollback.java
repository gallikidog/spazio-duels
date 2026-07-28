package network.minespazio.spazioduels.arena;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ArenaRollback {

    private final List<BlockState> placedBlockStates = new ArrayList<>();

    public void trackBlockPlace(Block block) {
        // Store original state before block was placed
        placedBlockStates.add(block.getState());
    }

    public void rollback() {
        // Rollback placed blocks in reverse order
        for (int i = placedBlockStates.size() - 1; i >= 0; i--) {
            BlockState state = placedBlockStates.get(i);
            state.update(true, false);
        }
        placedBlockStates.clear();
    }

    public void clear() {
        placedBlockStates.clear();
    }
}
