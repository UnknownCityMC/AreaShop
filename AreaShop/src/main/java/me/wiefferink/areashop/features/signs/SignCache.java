package me.wiefferink.areashop.features.signs;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.notify.UpdateRegionEvent;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignCache implements Listener {

    private final Map<String, RegionSign> allSigns = new HashMap<>();
    private final Map<String, List<RegionSign>> signsByChunk = new HashMap<>();

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, AreaShop.getInstance());
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
    public RegionSign getSignByLocation(Location location) {
        return allSigns.get(locationToString(location));
    }

    /**
     * Get the map with all signs.
     *
     * @return Map with all signs: locationString -&gt; RegionSign
     */
    public Map<String, RegionSign> getAllSigns() {
        return allSigns;
    }

    /**
     * Get the map with signs by chunk.
     *
     * @return Map with signs by chunk: chunkString -&gt; List&lt;RegionSign&gt;
     */
    public Map<String, List<RegionSign>> getSignsByChunk() {
        return signsByChunk;
    }

    public void addSign(RegionSign regionSign) {
        signsByChunk.computeIfAbsent(regionSign.getStringChunk(), (unused) -> new ArrayList<>()).add(regionSign);
    }

    public void removeSign(RegionSign regionSign) {
        final List<RegionSign> signs = signsByChunk.get(regionSign.getStringChunk());
        if (signs != null) {
            signs.remove(regionSign);
        }
        allSigns.remove(regionSign.getStringLocation());
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
        signsByChunk.clear();
        allSigns.clear();
    }

    @EventHandler
    public void regionUpdate(UpdateRegionEvent event) {
        event.getRegion().getSignsFeature().update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        List<RegionSign> chunkSigns = signsByChunk.get(chunkToString(event.getChunk()));
        if (chunkSigns == null) {
            return;
        }
        int batchSize = Math.max(chunkSigns.size() / 10, 10);
        Utils.runAsBatches(chunkSigns, batchSize, RegionSign::update, false);
    }

}
