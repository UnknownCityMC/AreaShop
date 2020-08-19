package me.wiefferink.areashop.tools;

import org.bukkit.Chunk;

public class ChunkUtils {

    private final long twoPower32 = Integer.MAX_VALUE + 1L;

    public static long getPackedChunkCoords(Chunk chunk) {
        return packToLong(chunk.getX(), chunk.getZ());
    }

    public static long packToLong(int primary, int secondary) {
        return (long) primary & 0xffffffffL | ((long) secondary & 0xffffffffL) << 32;
    }

    public static int getChunkX(long packed) {
        return (int) packed;
    }

    public static int getChunkZ(long packed) {
        return (int) (packed >> 32);
    }

    public static int[] getChunkCoords(long packed) {
        return new int[]{(int) packed, (int) (packed >> 32)};
    }
}
