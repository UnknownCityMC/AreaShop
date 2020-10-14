package me.wiefferink.areashop.features;

import co.aikar.taskchain.TaskChain;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.papermc.lib.PaperLib;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.signs.RegionSign;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.areashop.tools.Value;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TeleportFeature extends RegionFeature {

    public TeleportFeature() {
    }

    public TeleportFeature(GeneralRegion region) {
        setRegion(region);
    }

    /**
     * Checks if a certain location is safe to teleport to.
     *
     * @param location The location to check
     * @return true if it is safe, otherwise false
     */
    static boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block below = feet.getRelative(BlockFace.DOWN);
        Block above = head.getRelative(BlockFace.UP);

        // Check the block at the feet and head of the player
        if ((feet.getType().isSolid() && !canSpawnIn(feet.getType())) || feet.isLiquid()) {
            return false;
        } else if ((head.getType().isSolid() && !canSpawnIn(head.getType())) || head.isLiquid()) {
            return false;
        } else if (!below.getType().isSolid() || cannotSpawnOn(below.getType()) || below.isLiquid()) {
            return false;
        } else if (above.isLiquid() || cannotSpawnBeside(above.getType())) {
            return false;
        }

        // Get all blocks around the player (below foot level, foot level, head level and above head level)
        Set<Material> around = new HashSet<>();
        for (int y = 0; y <= 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // Skip blocks in the column of the player
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    around.add(below.getRelative(x, y, z).getType());
                }
            }
        }

        // Check the blocks around the player
        for (Material material : around) {
            if (cannotSpawnBeside(material)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a player can spawn in here.
     *
     * @param material Material to check (assumed that this is at the feet or head level)
     * @return true when it is safe to spawn inside, otherwise false
     */
    private static boolean canSpawnIn(Material material) {
        String name = material.name();
        return name.contains("DOOR")
                || name.contains("SIGN")
                || name.contains("PLATE") // Redstone plates
                || name.equals("DRAGON_EGG");
    }

    /**
     * Check if a player can spawn on here.
     *
     * @param material Material to check (assumed that this is below the feet)
     * @return true when it is safe to spawn on top of, otherwise false
     */
    private static boolean cannotSpawnOn(Material material) {
        String name = material.name();
        return name.equals("CACTUS")
                || name.contains("PISTON")
                || name.contains("SIGN")
                || name.contains("DOOR")
                || name.contains("PLATE")
                || name.contains("REDSTONE_LAMP")
                || name.contains("FENCE")
                || name.contains("GLASS_PANE") || name.contains("THIN_GLASS")
                || name.equals("DRAGON_EGG")
                || name.contains("MAGMA");
    }

    /**
     * Check if a player can spawn next to it.
     *
     * @param material Material to check (assumed that this is somewhere around the player)
     * @return true when it is safe to spawn next to, otherwise false
     */
    private static boolean cannotSpawnBeside(Material material) {
        String name = material.name();
        return name.contains("LAVA")
                || name.contains("CACTUS")
                || name.equals("FIRE")
                || name.contains("MAGMA");
    }

    /**
     * Get the teleportlocation set for this region.
     *
     * @return The teleport location, or null if not set
     */
    public Location getTeleportLocation() {
        return Utils.configToLocation(getRegion().getConfigurationSectionSetting("general.teleportLocation"));
    }

    /**
     * Check if the region has a teleportLocation specified.
     *
     * @return true if the region has a teleportlocation, false otherwise
     */
    public boolean hasTeleportLocation() {
        return getRegion().getConfigurationSectionSetting("general.teleportLocation") != null;
    }

    /**
     * Set the teleport location of this region.
     *
     * @param location The location to set as teleport location
     */
    public void setTeleport(Location location) {
        if (location == null) {
            getRegion().setSetting("general.teleportLocation", null);
        } else {
            getRegion().setSetting("general.teleportLocation", Utils.locationToConfig(location, true));
        }
    }

    /**
     * Teleport a player to the region or sign.
     *
     * @param player            Player that should be teleported
     * @param toSign            true to teleport to the first sign of the region, false for teleporting to the region itself
     * @param checkRestrictions Set to true if teleport permissions should be checked, false otherwise, also toggles cross-world check
     * @return true if the teleport succeeded, otherwise false
     */
    public CompletableFuture<Boolean> teleportPlayer(Player player, boolean toSign, boolean checkRestrictions) {
        return new TeleportJob(this, plugin).executeTeleport(player, toSign, checkRestrictions);
    }

    /**
     * Teleport a player to the region or sign when he has permissions for it.
     *
     * @param player Player that should be teleported
     * @param toSign true to teleport to the first sign of the region, false for teleporting to the region itself
     * @return true if the teleport succeeded, otherwise false
     */
    public CompletableFuture<Boolean> teleportPlayer(Player player, boolean toSign) {
        return teleportPlayer(player, toSign, true);
    }

    /**
     * Teleport a player to the region when he has permissions for it.
     *
     * @param player Player that should be teleported
     * @return true if the teleport succeeded, otherwise false
     */
    public CompletableFuture<Boolean> teleportPlayer(Player player) {
        return teleportPlayer(player, false, true);
    }

    /**
     * Get the start location of a safe teleport search.
     *
     * @param player The player to get it for
     * @param toSign true to try teleporting to the first sign, false for teleporting to the region
     * @return The start location
     */
    Location getStartLocation(Player player, Value<Boolean> toSign) {
        Location startLocation = null;
        ProtectedRegion worldguardRegion = getRegion().getRegion();

        // Try to get sign location
        List<RegionSign> signs = getRegion().getSignsFeature().getSigns();
        boolean signAvailable = !signs.isEmpty();
        if (toSign.get()) {
            if (signAvailable) {
                // Use the location 1 below the sign to prevent weird spawing above the sign
                startLocation = signs.get(0).getLocation(); //.subtract(0.0, 1.0, 0.0);
                startLocation.setPitch(player.getLocation().getPitch());
                startLocation.setYaw(player.getLocation().getYaw());

                // Move player x blocks away from the sign
                double distance = getRegion().getDoubleSetting("general.teleportSignDistance");
                if (distance > 0) {
                    BlockFace facing = getRegion().getSignsFeature().getSigns().get(0).getFacing();
                    Vector facingVector = new Vector(facing.getModX(), facing.getModY(), facing.getModZ())
                            .normalize()
                            .multiply(distance);
                    startLocation.setX(startLocation.getBlockX() + 0.5);
                    startLocation.setZ(startLocation.getBlockZ() + 0.5);
                    startLocation.add(facingVector);
                }
            } else {
                // No sign available
                getRegion().message(player, "teleport-changedToNoSign");
                toSign.set(false);
            }
        }

        // Use teleportation location that is set for the region
        if (startLocation == null && hasTeleportLocation()) {
            startLocation = getTeleportLocation();
        }

        // Calculate a default location
        if (startLocation == null) {
            // Set to block in the middle, y configured in the config
            Vector regionMin = AreaShop.getInstance().getWorldGuardHandler().getMinimumPoint(worldguardRegion);
            Vector regionMax = AreaShop.getInstance().getWorldGuardHandler().getMaximumPoint(worldguardRegion);
            Vector middle = regionMin.clone().midpoint(regionMax);
            String configSetting = getRegion().getStringSetting("general.teleportLocationY");
            if ("bottom".equalsIgnoreCase(configSetting)) {
                middle = middle.setY(regionMin.getBlockY());
            } else if ("top".equalsIgnoreCase(configSetting)) {
                middle = middle.setY(regionMax.getBlockY());
            } else if ("middle".equalsIgnoreCase(configSetting)) {
                middle = middle.setY(middle.getBlockY());
            } else {
                try {
                    int vertical = Integer.parseInt(configSetting);
                    middle = middle.setY(vertical);
                } catch (NumberFormatException e) {
                    AreaShop.warn("Could not parse general.teleportLocationY: '" + configSetting + "'");
                }
            }
            startLocation = new Location(getRegion().getWorld(), middle.getX(), middle.getY(), middle.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        }

        // Set location in the center of the block
        startLocation.setX(startLocation.getBlockX() + 0.5);
        startLocation.setZ(startLocation.getBlockZ() + 0.5);

        return startLocation;
    }


}
