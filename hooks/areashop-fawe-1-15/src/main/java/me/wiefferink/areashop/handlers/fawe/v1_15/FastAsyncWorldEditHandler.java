package me.wiefferink.areashop.handlers.fawe.v1_15;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.FaweLimit;
import com.boydti.fawe.object.RelightMode;
import com.boydti.fawe.object.clipboard.MemoryOptimizedClipboard;
import com.google.common.annotations.Beta;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.interfaces.WorldEditInterface;
import me.wiefferink.areashop.interfaces.WorldEditSelection;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

// Future: this class could use the schematic save/paste api of FAWE: https://github.com/boy0001/FastAsyncWorldedit/wiki/Pasting-a-schematic
// This would not allow for shaping the schematic to a polygon region though
public class FastAsyncWorldEditHandler extends WorldEditInterface {

    public FastAsyncWorldEditHandler(AreaShopInterface pluginInterface) {
        super(pluginInterface);
    }

    private static class RegionBoundMask implements Mask {

        private final BoundingBox boundingBox;

        public RegionBoundMask(final ProtectedRegion region) {
            final BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
            this.boundingBox = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
        }

        public RegionBoundMask(final BoundingBox boundingBox) {
            this.boundingBox = boundingBox.clone();
        }

        @Override
        public boolean test(BlockVector3 vector) {
            return boundingBox.contains(vector.getX(), vector.getY(), vector.getZ());
        }

        @Override
        public Mask2D toMask2D() {
            return null;
        }

        @Override
        public Mask copy() {
            return new RegionBoundMask(boundingBox);
        }
    }

    @Override
    @Beta
    public CompletableFuture<Boolean> restoreRegionBlocksAsync(File rawFile, GeneralRegionInterface regionInterface) {
        File file = null;
        ClipboardFormat format = null;
        for (ClipboardFormat formatOption : BuiltInClipboardFormat.values()) {
            if (new File(rawFile.getAbsolutePath() + "." + formatOption.getPrimaryFileExtension())
                    .exists()) {
                file = new File(
                        rawFile.getAbsolutePath() + "." + formatOption.getPrimaryFileExtension());
                format = formatOption;
                break;
            }
        }
        if (file == null) {
            pluginInterface.getLogger().info("Did not restore region " + regionInterface.getName()
                    + ", schematic file does not exist: " + rawFile.getAbsolutePath());
            return CompletableFuture.completedFuture(false);
        }
        pluginInterface.debugI("Trying to restore region", regionInterface.getName(), " from file",
                file.getAbsolutePath(), "with format", format.getName());

        com.sk89q.worldedit.world.World world = null;
        if (regionInterface.getName() != null) {
            world = BukkitAdapter.adapt(regionInterface.getWorld());
        }
        if (world == null) {
            pluginInterface.getLogger().info(
                    "Did not restore region " + regionInterface.getName() + ", world not found: "
                            + regionInterface.getWorldName());
            return CompletableFuture.completedFuture(false);
        }
        final CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        final FaweLimit limit = new FaweLimit();
        limit.MAX_CHANGES = pluginInterface.getConfig().getInt("maximumBlocks");
        EditSession editSession = FaweAPI.getEditSessionBuilder(world).limit(limit).relightMode(RelightMode.OPTIMAL).build();
        editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
        ProtectedRegion region = regionInterface.getRegion();
        final RegionType regionType = region.getType();
        // Get the origin and size of the region
        final BlockVector3 origin = BlockVector3
                .at(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(),
                        region.getMinimumPoint().getBlockZ());
        final RegionBoundMask mask = new RegionBoundMask(region);
        final File finalFile = file;
        final String regionName = regionInterface.getName();
        final double regionWidth = regionInterface.getWidth(), regionHeight = regionInterface.getHeight(), regionDepth = regionInterface.getDepth();
        final LocalSession session =
                new LocalSession(pluginInterface.getWorldEdit().getLocalConfiguration());
        FaweAPI.getTaskManager().async(() -> {
            // Read the schematic and paste it into the world
            try (Closer closer = Closer.create()) {
                FileInputStream fis = closer.register(new FileInputStream(finalFile));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(bis));
                ClipboardReader reader = new FastSchematicReader(nbtStream);

                Clipboard clipboard = reader.read();
                if (clipboard.getDimensions().getY() != regionHeight
                        || clipboard.getDimensions().getX() != regionWidth
                        || clipboard.getDimensions().getZ() != regionDepth) {
                    pluginInterface.getLogger().warning(
                            "Size of the region " + regionName
                                    + " is not the same as the schematic to restore!");
                    pluginInterface.debugI(
                            "schematic|region, x:" + clipboard.getDimensions().getX() + "|"
                                    + regionWidth + ", y:" + clipboard.getDimensions().getY()
                                    + "|" + regionHeight + ", z:" + clipboard.getDimensions()
                                    .getZ() + "|" + regionDepth);
                }
                clipboard.setOrigin(clipboard.getMinimumPoint());
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
                session.setBlockChangeLimit(limit.MAX_CHANGES);
                session.setClipboard(clipboardHolder);

                // Build operation

                BlockTransformExtent extent = new BlockTransformExtent(clipboard);
                ForwardExtentCopy copy =
                        new ForwardExtentCopy(extent, clipboard.getRegion(), clipboard.getOrigin(),
                                editSession, origin);
                copy.setTransform(clipboardHolder.getTransform());
                // Mask to region (for polygon and other weird shaped regions)
                // TODO make this more efficient (especially for polygon regions)
                if (regionType != RegionType.CUBOID) {
                    copy.setSourceMask(mask);
                }
                Operations.completeLegacy(copy);
            } catch (MaxChangedBlocksException e) {
                pluginInterface.getLogger().warning(
                        "exceeded the block limit while restoring schematic of " + regionInterface.getName()
                                + ", limit in exception: " + e.getBlockLimit() + ", limit passed by AreaShop: "
                                + pluginInterface.getConfig().getInt("maximumBlocks"));
                CompletableFuture.completedFuture(false);
            } catch (IOException e) {
                pluginInterface.getLogger().warning(
                        "An error occured while restoring schematic of " + regionInterface.getName()
                                + ", enable debug to see the complete stacktrace");
                pluginInterface.debugI(ExceptionUtils.getStackTrace(e));
                CompletableFuture.completedFuture(false);
            } catch (Exception e) {
                pluginInterface.getLogger()
                        .warning("crashed during restore of " + regionInterface.getName());
                pluginInterface.debugI(ExceptionUtils.getStackTrace(e));
                CompletableFuture.completedFuture(false);
            }
            editSession.flushQueue();
            CompletableFuture.completedFuture(true);
        });
        return completableFuture;
    }

    @Override
    public boolean restoreRegionBlocks(File rawFile, GeneralRegionInterface regionInterface) {
        return restoreRegionBlocksAsync(rawFile, regionInterface).join();
    }

    @Override
    @Beta
    public CompletableFuture<Boolean> saveRegionBlocksAsync(File file, GeneralRegionInterface regionInterface) {
        // TODO implement using the FastAsyncWorldEdit api to save async
        ClipboardFormat format = BuiltInClipboardFormat.MINECRAFT_STRUCTURE;
        // TODO allow selecting FAWE format in the config? (when enabled you cannot go back to vanilla WorldEdit easily)

        file = new File(file.getAbsolutePath() + "." + format.getPrimaryFileExtension());
        pluginInterface.debugI("Trying to save region", regionInterface.getName(), " to file",
                file.getAbsolutePath(), "with format", format.getName());
        com.sk89q.worldedit.world.World world = null;
        if (regionInterface.getWorld() != null) {
            world = BukkitAdapter.adapt(regionInterface.getWorld());
        }
        if (world == null) {
            pluginInterface.getLogger().warning(
                    "Did not save region " + regionInterface.getName() + ", world not found: "
                            + regionInterface.getWorldName());
            return CompletableFuture.completedFuture(false);
        }
        final FaweLimit limit = new FaweLimit();
        limit.MAX_CHANGES = pluginInterface.getConfig().getInt("maximumBlocks");
        EditSession editSession = FaweAPI.getEditSessionBuilder(world).relightMode(RelightMode.OPTIMAL).limit(limit).build();

        final ProtectedRegion region = regionInterface.getRegion();
        final BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
        final String regionName = regionInterface.getName();
        final World finalWorld = world;
        final CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        final File finalFile = file;
        FaweAPI.getTaskManager().async(() -> {
            // Create a clipboard
            CuboidRegion selection =
                    new CuboidRegion(finalWorld, min, max);
            Clipboard clipboard = new MemoryOptimizedClipboard(selection);
            clipboard.setOrigin(min);
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession,
                    selection.clone(), clipboard,
                    min);
            try {
                Operations.completeLegacy(copy);
            } catch (MaxChangedBlocksException e) {
                pluginInterface.getLogger().warning(
                        "Exceeded the block limit while saving schematic of " + regionName
                                + ", limit in exception: " + e.getBlockLimit() + ", limit passed by AreaShop: "
                                + pluginInterface.getConfig().getInt("maximumBlocks"));
                completableFuture.complete(false);
            }

            try (Closer closer = Closer.create()) {
                FileOutputStream fos = closer.register(new FileOutputStream(finalFile));
                BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
                ClipboardWriter writer = closer.register(format.getWriter(bos));
                writer.write(clipboard);
            } catch (IOException e) {
                pluginInterface.getLogger().warning(
                        "An error occured while saving schematic of " + regionName
                                + ", enable debug to see the complete stacktrace");
                pluginInterface.debugI(ExceptionUtils.getStackTrace(e));
                completableFuture.complete(false);
            } catch (Exception e) {
                pluginInterface.getLogger()
                        .warning("Crashed during save of " + regionName);
                pluginInterface.debugI(ExceptionUtils.getStackTrace(e));
               completableFuture.complete(false);
            }
            completableFuture.complete(true);
        });
        return completableFuture;
    }

    @Override
    public boolean saveRegionBlocks(File file, GeneralRegionInterface regionInterface) {
        return saveRegionBlocksAsync(file, regionInterface).join();
    }

    @Override
    public WorldEditSelection getPlayerSelection(Player player) {
        try {
            Region region = pluginInterface.getWorldEdit().getSession(player)
                    .getSelection(BukkitAdapter.adapt(player.getWorld()));
            return new WorldEditSelection(player.getWorld(),
                    BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint()),
                    BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint()));
        } catch (IncompleteRegionException e) {
            return null;
        }
    }
}
