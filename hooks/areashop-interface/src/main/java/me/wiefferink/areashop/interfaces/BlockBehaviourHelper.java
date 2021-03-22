package me.wiefferink.areashop.interfaces;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

public interface BlockBehaviourHelper {

    default boolean canPlace(Location location, Material material) {
        return canPlace(location, material.createBlockData());
    }

    boolean canPlace(Location location, BlockData blockData);

    /**
     * Check if a sign is valid.
     *
     * @param block The block
     * @return Returns true if the sign can exist in that location, false otherwise.
     */
    boolean isBlockValid(Block block);

    boolean isBlockStateValid(BlockState blockState);

}
