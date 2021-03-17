package me.wiefferink.areashop.interfaces;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public interface BlockBehaviourHelper {

    boolean canPlace(Location location, Material material);

    /**
     * Check if a sign is valid.
     *
     * @param block The block
     * @return Returns true if the sign can exist in that location, false otherwise.
     */
    boolean isBlockValid(Block block);

}
