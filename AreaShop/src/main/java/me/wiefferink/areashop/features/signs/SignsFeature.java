package me.wiefferink.areashop.features.signs;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.RegionFeature;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignsFeature extends RegionFeature {

    public static final SignCache signCache = new SignCache();

    static {
        signCache.registerListeners();
    }

    private Map<String, RegionSign> signs;

    public SignsFeature() {

    }

    /**
     * Constructor.
     *
     * @param region The region to bind to
     */
    public SignsFeature(LegacyGeneralRegion region) {
        setRegion(region);
        signs = new HashMap<>();
        // Setup current signs
        ConfigurationSection signSection = region.getConfig().getConfigurationSection("general.signs");
        if (signSection != null) {
            for (String signKey : signSection.getKeys(false)) {
                RegionSign sign = new RegionSign(this, signKey);
                Location location = sign.getLocation();
                if (location == null) {
                    AreaShop.warn("Sign with key " + signKey + " of region " + region.getRegionId() + " does not have a proper location");
                    continue;
                }
                signs.put(sign.getStringLocation(), sign);
                signCache.addSign(sign);
            }
        }
    }

    public static void shutdownGlobalState() {
        signCache.shutdown();
    }

    /**
     * Convert a location to a string to use as map key.
     *
     * @param location The location to get the key for
     * @return A string to use in a map for a location
     */
    public static String locationToString(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    /**
     * Convert a chunk to a string to use as map key.
     *
     * @param location The location to get the key for
     * @return A string to use in a map for a chunk
     */
    public static String chunkToString(Location location) {
        return location.getWorld().getName() + ";" + (location.getBlockX() >> 4) + ";" + (location.getBlockZ() >> 4);
    }

    /**
     * Convert a chunk to a string to use as map key.
     * Use a Location argument to prevent chunk loading!
     *
     * @param chunk The location to get the key for
     * @return A string to use in a map for a chunk
     */
    public static String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    /**
     * Get a sign by a location.
     *
     * @param location The location to get the sign for
     * @return The RegionSign that is at the location, or null if none
     */
    public static RegionSign getSignByLocation(Location location) {
        return signCache.getSignByLocation(location);
    }

    /**
     * Get the map with all signs.
     *
     * @return Map with all signs: locationString -&gt; RegionSign
     */
    public static Map<String, RegionSign> getAllSigns() {
        return signCache.getAllSigns();
    }

    /**
     * Get the map with signs by chunk.
     *
     * @return Map with signs by chunk: chunkString -&gt; List&lt;RegionSign&gt;
     */
    public static Map<String, List<RegionSign>> getSignsByChunk() {
        return signCache.getSignsByChunk();
    }

    @Override
    public void shutdown() {
        // Deregister signs from the registry
        if (signs != null) {
            for (Map.Entry<String, RegionSign> entry : signs.entrySet()) {
                signCache.removeSign(entry.getValue());
            }
        }
    }

    /**
     * Update all signs connected to this region.
     *
     * @return true if all signs are updated correctly, false if one or more updates failed
     */
    public boolean update() {
        boolean result = true;
        for (RegionSign sign : signs.values()) {
            result &= sign.update();
        }
        return result;
    }

    /**
     * Check if any of the signs need periodic updating.
     *
     * @return true if one or more of the signs need periodic updating, otherwise false
     */
    public boolean needsPeriodicUpdate() {
        for (RegionSign sign : signs.values()) {
            if (sign.needsPeriodicUpdate()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the signs of this region.
     *
     * @return List of signs
     */
    public List<RegionSign> getSigns() {
        return new ArrayList<>(signs.values());
    }

    /**
     * Get the signs of this region.
     *
     * @return Map with signs: locationString -&gt; RegionSign
     */
    Map<String, RegionSign> getSignsRef() {
        return signs;
    }

    /**
     * Get a list with all sign locations.
     *
     * @return A List with all sign locations
     */
    public List<Location> getSignLocations() {
        List<Location> result = new ArrayList<>();
        for (RegionSign sign : signs.values()) {
            result.add(sign.getLocation());
        }
        return result;
    }

    /**
     * Add a sign to this region.
     *
     * @param location The location of the sign
     * @param signType The type of the sign (WALL_SIGN or SIGN_POST)
     * @param facing   The orientation of the sign
     * @param profile  The profile to use with this sign (null for default)
     */
    public void addSign(Location location, Material signType, BlockFace facing, String profile) {
        int i = 0;
        while (getRegion().getConfig().isSet("general.signs." + i)) {
            i++;
        }
        String signPath = "general.signs." + i + ".";
        final LegacyGeneralRegion region = getRegion();
        region.setSetting(signPath + "location", Utils.locationToConfig(location));
        region.setSetting(signPath + "facing", facing != null ? facing.name() : null);
        region.setSetting(signPath + "signType", signType != null ? signType.name() : null);
        if (profile != null && !profile.isEmpty()) {
            region.setSetting(signPath + "profile", profile);
        }
        // Add to the map
        RegionSign sign = new RegionSign(this, String.valueOf(i));
        signs.put(sign.getStringLocation(), sign);
        signCache.addSign(sign);
    }

}
