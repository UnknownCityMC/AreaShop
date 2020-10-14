package me.wiefferink.areashop.features;

import co.aikar.taskchain.TaskChain;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.areashop.tools.Value;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

class TeleportJob {

    private final AreaShop plugin;
    private final GeneralRegion region;
    private final TeleportFeature feature;

    private boolean running;
    private int checked;
    private boolean done;
    private boolean blocksInRegionCopy;
    private boolean northDone = false, eastDone = false, southDone = false, westDone = false, topDone = false, bottomDone = false;
    private boolean continueThisDirection;
    private int radius = 1;

    public TeleportJob(final TeleportFeature region, final AreaShop plugin) {
        this.feature = region;
        this.region = region.getRegion();
        this.plugin = plugin;
    }

    private static Function<double[], Boolean> generateContainsFunction(final ProtectedRegion worldguardRegion) {
        final BlockVector3 min = worldguardRegion.getMinimumPoint(), max = worldguardRegion.getMaximumPoint();
        if (worldguardRegion instanceof GlobalProtectedRegion) {
            return (unused) -> true;
        } else if (worldguardRegion instanceof ProtectedCuboidRegion) {
            return coords -> {
                final double x = coords[0];
                final double y = coords[1];
                final double z = coords[2];
                return x >= min.getBlockX() && x < max.getBlockX() + 1
                        && y >= min.getBlockY() && y < max.getBlockY() + 1
                        && z >= min.getBlockZ() && z < max.getBlockZ() + 1;
            };
        } else if (worldguardRegion instanceof ProtectedPolygonalRegion) {
            final int minY = min.getY(), maxY = max.getY();
            final List<BlockVector2> points = worldguardRegion.getPoints();
            return coords -> {

                int targetX = (int) Math.round(coords[0]); // Width
                int targetY = (int) Math.round(coords[1]); // Height
                int targetZ = (int) Math.round(coords[2]); // Depth

                if (targetY < minY || targetY > maxY) {
                    return false;
                }
                //Quick and dirty check.
                if (targetX < min.getBlockX() || targetX > max.getBlockX() || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
                    return false;
                }
                boolean inside = false;
                int npoints = points.size();
                int xNew, zNew;
                int xOld, zOld;
                int x1, z1;
                int x2, z2;
                long crossproduct;
                int i;

                xOld = points.get(npoints - 1).getBlockX();
                zOld = points.get(npoints - 1).getBlockZ();

                for (i = 0; i < npoints; i++) {
                    xNew = points.get(i).getBlockX();
                    zNew = points.get(i).getBlockZ();
                    //Check for corner
                    if (xNew == targetX && zNew == targetZ) {
                        return true;
                    }
                    if (xNew > xOld) {
                        x1 = xOld;
                        x2 = xNew;
                        z1 = zOld;
                        z2 = zNew;
                    } else {
                        x1 = xNew;
                        x2 = xOld;
                        z1 = zNew;
                        z2 = zOld;
                    }
                    if (x1 <= targetX && targetX <= x2) {
                        crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1)
                                - ((long) z2 - (long) z1) * (long) (targetX - x1);
                        if (crossproduct == 0) {
                            if ((z1 <= targetZ) == (targetZ <= z2)) return true; // on edge
                        } else if (crossproduct < 0 && (x1 != targetX)) {
                            inside = !inside;
                        }
                    }
                    xOld = xNew;
                    zOld = zNew;
                }

                return inside;
            };
        } else {
            throw new UnsupportedOperationException("Unknown region type: " + worldguardRegion.getClass().getCanonicalName());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public CompletableFuture<Boolean> executeTeleport(final Player player, boolean toSign, boolean checkRestrictions) {
        if (running) {
            throw new IllegalStateException("Task is already running!");
        }
        running = true;
        // Check basics
        if (feature == null) {
            region.message(player, "general-noRegion");
            return CompletableFuture.completedFuture(false);
        }

        if (region.getWorld() == null) {
            region.message(player, "general-noWorld");
            return CompletableFuture.completedFuture(false);
        }

        if (checkRestrictions) {
            // Check correct world
            if (!region.getBooleanSetting("general.teleportCrossWorld") && !player.getWorld().equals(region.getWorld())) {
                region.message(player, "teleport-wrongWorld", player.getWorld().getName());
                return CompletableFuture.completedFuture(false);
            }

            boolean owner = player.getUniqueId().equals(region.getOwner());
            boolean friend = region.getFriendsFeature().getFriends().contains(player.getUniqueId());
            boolean available = region.isAvailable();
            // Teleport to sign instead if they dont have permission for teleporting to region
            if ((!toSign && owner && !player.hasPermission("areashop.teleport") && player.hasPermission("areashop.teleportsign")
                    || !toSign && !owner && !friend && !player.hasPermission("areashop.teleportall") && player.hasPermission("areashop.teleportsignall")
                    || !toSign && !owner && friend && !player.hasPermission("areashop.teleportfriend") && player.hasPermission("areashop.teleportfriendsign")
                    || !toSign && !owner && !friend && available && !player.hasPermission("areashop.teleportavailable") && player.hasPermission("areashop.teleportavailablesign"))) {
                region.message(player, "teleport-changedToSign");
                toSign = true;
            }
            // Check permissions
            if (owner && !available && !player.hasPermission("areashop.teleport") && !toSign) {
                region.message(player, "teleport-noPermission");
                return CompletableFuture.completedFuture(false);
            } else if (!owner && !available && !player.hasPermission("areashop.teleportall") && !toSign && !friend) {
                region.message(player, "teleport-noPermissionOther");
                return CompletableFuture.completedFuture(false);
            } else if (!owner && !available && !player.hasPermission("areashop.teleportfriend") && !toSign && friend) {
                region.message(player, "teleport-noPermissionFriend");
                return CompletableFuture.completedFuture(false);
            } else if (available && !player.hasPermission("areashop.teleportavailable") && !toSign) {
                region.message(player, "teleport-noPermissionAvailable");
                return CompletableFuture.completedFuture(false);
            } else if (owner && !available && !player.hasPermission("areashop.teleportsign") && toSign) {
                region.message(player, "teleport-noPermissionSign");
                return CompletableFuture.completedFuture(false);
            } else if (!owner && !available && !player.hasPermission("areashop.teleportsignall") && toSign && !friend) {
                region.message(player, "teleport-noPermissionOtherSign");
                return CompletableFuture.completedFuture(false);
            } else if (!owner && !available && !player.hasPermission("areashop.teleportfriendsign") && toSign && friend) {
                region.message(player, "teleport-noPermissionFriendSign");
                return CompletableFuture.completedFuture(false);
            } else if (available && !player.hasPermission("areashop.teleportavailablesign") && toSign) {
                region.message(player, "teleport-noPermissionAvailableSign");
                return CompletableFuture.completedFuture(false);
            }
        }

        // Get the starting location
        Value<Boolean> toSignRef = new Value<>(toSign);
        Location startLocation = feature.getStartLocation(player, toSignRef);
        toSign = toSignRef.get();

        boolean insideRegion;
        if (toSign) {
            insideRegion = region.getBooleanSetting("general.teleportToSignIntoRegion");
        } else {
            insideRegion = region.getBooleanSetting("general.teleportIntoRegion");
        }

        // Check locations starting from startLocation and then a cube that increases
        // radius around that (until no block in the region is found at all cube sides)

        ProtectedRegion worldguardRegion = region.getRegion();
        final Function<double[], Boolean> regionContains = generateContainsFunction(worldguardRegion);

        boolean blocksInRegion = worldguardRegion.contains(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
        if (!blocksInRegion && insideRegion) {
            region.message(player, "teleport-blocked");
            return CompletableFuture.completedFuture(false);
        }

        // Tracking of which sides to continue the search
        final World world = region.getWorld();
        BlockVector3 min = worldguardRegion.getMinimumPoint();
        BlockVector3 max = worldguardRegion.getMaximumPoint();

        player.sendMessage("Loading Chunks Phase 0");
        final Collection<Chunk> collection = ConcurrentHashMap.newKeySet();
        final List<CompletableFuture<Object>> futures = new ArrayList<>();
        final Collection<int[]> coords = new HashSet<>();
        for (int x = min.getX(); x < max.getX(); x += 16) {
            for (int z = min.getZ(); z < max.getZ(); z += 16) {
                coords.add(new int[]{x, z});
            }
        }
        final CompletableFuture<?> schedulingFuture;
        if (coords.size() < 128) {
            schedulingFuture = Utils.runAsBatches(coords, 32,
                    coord -> futures.add(PaperLib.getChunkAtAsync(world, coord[0], coord[1]).thenApply(chunk -> {
                        chunk.setForceLoaded(true);
                        collection.add(chunk);
                        return chunk;
                    })), false);
        } else {
            schedulingFuture = null;
        }
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        final boolean finalToSign = toSign;

        final Runnable task = () -> {
            blocksInRegionCopy = blocksInRegion;
            int safeY = startLocation.getBlockY();
            int safeX = startLocation.getBlockX();
            int safeZ = startLocation.getBlockZ();
            final Location temp = new Location(world, 0, 0, 0);
            // Tries limit tracking

            int maxTries = plugin.getConfig().getInt("maximumTries");

            // Tracking of which sides to continue the search
            this.done = TeleportFeature.isSafe(startLocation);
            Utils.newChain().async(() -> {
                while ((blocksInRegionCopy || !insideRegion) && !done) {
                    blocksInRegionCopy = false;

                    // North side
                    continueThisDirection = false;
                    temp.setX(-radius + 1);
                    temp.setY(-radius + 1);
                    temp.setZ(-radius);
                    for (int x = -radius + 1; x <= radius && !done && !northDone; x++) {
                        for (int y = -radius + 1; y < radius && !done; y++) {
                            if (y + safeY > 256 || y + safeY < 0) {
                                continue;
                            }
                            if (!insideRegion || regionContains.apply(new double[]{x + safeX, y + safeY, safeZ - radius})) {
                                checked++;
                                done = TeleportFeature.isSafe(temp) || checked > maxTries;
                                blocksInRegionCopy = true;
                                continueThisDirection = true;
                            }
                            temp.add(0, 1, 0);
                        }
                        temp.add(1, 0, 0);
                    }
                    northDone = northDone || !continueThisDirection;
                }
            }).abortIf((var0) -> done).async(() -> {
                // South side
                continueThisDirection = false;
                temp.setX(radius - 1);
                temp.setY(-radius + 1);
                temp.setZ(-radius);
                for (int x = radius - 1; x >= -radius && !done && !southDone; x--) {
                    for (int y = -radius + 1; y < radius && !done; y++) {
                        if (y + safeY > 256 || y + safeY < 0) {
                            continue;
                        }
                        if (!insideRegion || regionContains.apply(new double[]{safeX + x, safeY + y, safeZ - radius})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 1, 0);
                    }
                    temp.add(-1, 0, 0);
                }
                southDone = southDone || !continueThisDirection;
            }).abortIf((var0) -> done).async(() -> {
                // East side
                continueThisDirection = false;
                temp.setX(-radius + 1);
                temp.setY(-radius + 1);
                temp.setZ(0);
                for (int z = -radius + 1; z <= radius && !done && !eastDone; z++) {
                    for (int y = -radius + 1; y < radius && !done; y++) {
                        if (y + safeY > 256 || y + safeY < 0) {
                            continue;
                        }
                        if (!insideRegion || regionContains.apply(new double[]{safeX + radius, y + safeY, z + safeZ})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 1, 0);
                    }
                    temp.add(0, 0, 1);
                }
                eastDone = eastDone || !continueThisDirection;
            }).abortIf((var0) -> done).async(() -> {
                // West side
                continueThisDirection = false;
                temp.setX(-radius);
                temp.setY(-radius);
                temp.setZ(radius - 1);
                for (int z = radius - 1; z >= -radius && !done && !westDone; z--) {
                    for (int y = -radius + 1; y < radius && !done; y++) {
                        if (y + safeY > 256 || y + safeY < 0) {
                            continue;
                        }
                        if (!insideRegion || regionContains.apply(new double[]{safeX - radius, safeY + y, safeZ + z})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 1, 0);
                    }
                    temp.add(0, 0, -1);
                }
                westDone = westDone || !continueThisDirection;
            }).abortIf((var0) -> done).async(() -> {
                // Top side
                continueThisDirection = false;
                // Middle block of the top
                if ((safeY + radius) > 256) {
                    topDone = true;
                }
                if (!done && !topDone) {
                    temp.setX(0);
                    temp.setY(radius);
                    temp.setZ(0);
                    if (!insideRegion || regionContains.apply(new double[]{safeX, safeY + radius, safeZ})) {
                        checked++;
                        try {
                            this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                            ex.printStackTrace();
                        }
                        blocksInRegionCopy = true;
                        continueThisDirection = true;
                    }
                }

                temp.setY(radius);
                for (int r = 1; r <= radius && !done && !topDone; r++) {
                    temp.setX(-r + 1);
                    temp.setZ(-r);
                    for (int x = -r + 1; x <= r && !done; x++) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + x, safeY + radius, safeZ - r})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(1, 0, 0);
                    }
                    // East
                    temp.setX(r);
                    temp.setZ(-r + 1);
                    for (int z = -r + 1; z <= r && !done; z++) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + r, safeY + radius, safeZ + z})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 0, z);
                    }
                    // South side
                    temp.setX(r - 1);
                    temp.setZ(r);
                    for (int x = r - 1; x >= -r && !done; x--) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + x, safeY + radius, safeZ + r})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(-1, 0, 0);
                    }
                    // West side
                    temp.setX(-r);
                    temp.setZ(r - 1);
                    for (int z = r - 1; z >= -r && !done; z--) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX - r, safeY + radius, safeZ + z})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 0, -1);
                    }
                }
                topDone = topDone || !continueThisDirection;
            }).abortIf(var0 -> done).async(() -> {
                // Bottom side
                continueThisDirection = false;
                // Middle block of the bottom
                if (safeY - radius < 0) {
                    bottomDone = true;
                }
                if (!done && !bottomDone) {
                    temp.setX(0);
                    temp.setY(-radius);
                    temp.setZ(0);
                    if (!insideRegion || regionContains.apply(new double[]{safeX, safeY - radius, safeZ})) {
                        checked++;
                        try {
                            this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                            ex.printStackTrace();
                        }
                        blocksInRegionCopy = true;
                        continueThisDirection = true;
                    }
                }
                temp.setY(-radius);
                for (int r = 1; r <= radius && !done && !bottomDone; r++) {
                    // North
                    temp.setX(-r + 1);
                    temp.setZ(-r);
                    for (int x = -r + 1; x <= r && !done; x++) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + x, safeY - radius, safeZ - r})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(1, 0, 0);
                    }
                    // East
                    temp.setX(0);
                    temp.setZ(-r + 1);
                    for (int z = -r + 1; z <= r && !done; z++) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + r, safeY - radius, safeZ + z})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 0, 1);
                    }
                    // South side
                    temp.setX(r - 1);
                    temp.setZ(r);
                    for (int x = r - 1; x >= -r && !done; x--) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX + x, safeY - radius, safeZ + r})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(-1, 0, 0);
                    }
                    // West side
                    temp.setX(-r);
                    temp.setZ(r - 1);
                    for (int z = r - 1; z >= -r && !done; z--) {
                        if (!insideRegion || regionContains.apply(new double[]{safeX - r, safeY - radius, safeZ + z})) {
                            checked++;
                            try {
                                this.done = Bukkit.getScheduler().callSyncMethod(plugin, () -> TeleportFeature.isSafe(temp) || checked > maxTries).get(5, TimeUnit.SECONDS);
                            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                                ex.printStackTrace();
                            }
                            blocksInRegionCopy = true;
                            continueThisDirection = true;
                        }
                        temp.add(0, 0, -1);
                    }
                }
                bottomDone = bottomDone || !continueThisDirection;

                // Increase cube radius
                radius++;
                startLocation.add(temp);
            }).execute(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                if (done && TeleportFeature.isSafe(startLocation)) {
                    if (finalToSign) {
                        region.message(player, "teleport-successSign");

                        // Let the player look at the sign
                        Vector playerVector = startLocation.toVector();
                        playerVector.setY(playerVector.getY() + player.getEyeHeight(true));
                        Vector signVector = region.getSignsFeature().getSigns().get(0).getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
                        Vector direction = playerVector.clone().subtract(signVector).normalize();
                        startLocation.setYaw(180 - (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())));
                        startLocation.setPitch(90 - (float) Math.toDegrees(Math.acos(direction.getY())));
                    } else {
                        region.message(player, "teleport-success");
                    }

                    AreaShop.debug("Found location: " + startLocation.toString() + " Tries: " + checked);
                    PaperLib.teleportAsync(player, startLocation).thenAccept(future::complete);
                } else {
                    region.message(player, "teleport-noSafe", checked, maxTries);
                    AreaShop.debug("No location found, checked " + checked + " spots of max " + maxTries);
                    future.complete(false);
                }
            }));
        };
        TaskChain<?> chain = Utils.newChain();
        if (schedulingFuture != null) {
            chain = chain.asyncFirstFuture(() -> schedulingFuture).asyncFutures((unused) -> futures);
        }
        chain.async(task::run).execute();
        return future.thenApply((unused0) -> {
            for (Chunk chunk : collection) {
                chunk.setForceLoaded(false);
            }
            running = false;
            return unused0;
        });
    }
}
