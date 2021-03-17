package me.wiefferink.areashop.nms.v1_15_R1;

import me.wiefferink.areashop.interfaces.BlockBehaviourHelper;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

import java.util.Objects;

public class BlockBehaviourHelperImpl implements BlockBehaviourHelper {

    @Override
    public boolean canPlace(Location location, Material material) {
        Objects.requireNonNull(location.getWorld(), "Null World!");
        final BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        final CraftWorld craftWorld = (CraftWorld) location.getWorld();
        final World world = craftWorld.getHandle();
        final CraftBlockData craftBlockData = (CraftBlockData) material.createBlockData();
        final IBlockData ibd = craftBlockData.getState();
        return ibd.canPlace(world, blockPosition);
    }

    @Override
    public boolean isBlockValid(Block block) {
        CraftBlock craftBlock = (CraftBlock) block;
        CraftWorld craftWorld = (CraftWorld) craftBlock.getWorld();
        World world = craftWorld.getHandle();
        IBlockData ibd = craftBlock.getNMS();
        return ibd.canPlace(world, craftBlock.getPosition());
    }

}
