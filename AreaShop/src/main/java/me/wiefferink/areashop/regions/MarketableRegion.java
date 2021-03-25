package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.interfaces.IRegion;
import me.wiefferink.areashop.regions.util.*;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface MarketableRegion extends IRegion, Comparable<MarketableRegion>, ReplacementProvider {
    /**
     * Get the amount of regions a player has matching a certain limits group (config.yml -- limitGroups)
     *
     * @param player     The player to check the amount for
     * @param limitGroup The group to check
     * @param regions    All the regions a player has bought or rented
     * @param exclude    Exclude this region from the count
     * @return The number of regions that the player has bought or rented matching the limit group (worlds and groups filters)
     */
    static int hasRegionsInLimitGroup(OfflinePlayer player, String limitGroup, List<? extends MarketableRegion> regions, MarketableRegion exclude) {
        int result = 0;
        for (MarketableRegion region : regions) {
            final RegionMeta meta = region.regionMeta();
            if (meta.countForLimits()
                    // && region.isOwner(player)
                    && region.matchesLimitGroup(limitGroup)
                    && (exclude == null || !exclude.getRegionId().equals(region.getRegionId()))) {
                result++;
            }
        }
        return result;
    }

    @NotNull RegionMeta regionMeta();

    @NotNull RegionFeatureHelper featureHelper();

    /**
     * Shared setup of all constructors.
     */
    void setup();

    /**
     * Deregister everything.
     */
    void destroy();

    /**
     * Broadcast an event to indicate that region settings have been changed.
     * This will update region flags, signs, etc.
     */
    void update();

    /**
     * Broadcast the given event and update the region status.
     *
     * @param event The update event that should be broadcasted
     */
    void notifyAndUpdate(NotifyRegionEvent<?> event);

    /**
     * Check if the region has been deleted.
     *
     * @return true if the region has been deleted, otherwise false
     */
    boolean isDeleted();

    /**
     * Indicate that this region has been deleted.
     */
    void delete();

    /**
     * Get the minimum corner of the region.
     *
     * @return Vector
     */
    Vector getMinimumPoint();

    /**
     * Get the maximum corner of the region.
     *
     * @return Vector
     */
    Vector getMaximumPoint();

    /**
     * Get the groups that this region is added to.
     *
     * @return A Set with all groups of this region
     */
    Set<RegionGroup> getGroups();

    /**
     * Get a list of names from groups this region is in.
     *
     * @return A list of groups this region is part of
     */
    List<String> getGroupNames();

    /**
     * Method to send a message to a CommandSender, using chatprefix if it is a player.
     * Automatically includes the region in the message, enabling the use of all variables.
     *
     * @param target The CommandSender you wan't to send the message to (e.g. a player)
     * @param key    The key to get the translation
     * @param prefix Specify if the message should have a prefix
     * @param params The parameters to inject into the message string
     */
    void configurableMessage(Object target, String key, boolean prefix, Object... params);

    void messageNoPrefix(Object target, String key, Object... params);

    void message(Object target, String key, Object... params);

    /**
     * Save all blocks in a region for restoring later.
     *
     * @param fileName The name of the file to save to (extension and folder will be added)
     * @return true if the region has been saved properly, otherwise false
     */
    boolean saveRegionBlocks(String fileName);

    /**
     * Save all blocks in a region for restoring later.
     *
     * @param fileName The name of the file to save to (extension and folder will be added)
     * @return true if the region has been saved properly, otherwise false
     */
    CompletableFuture<Boolean> saveRegionBlocksAsync(String fileName);

    /**
     * Restore all blocks in a region for restoring later.
     *
     * @param fileName The name of the file to save to (extension and folder will be added)
     * @return true if the region has been restored properly, otherwise false
     */
    boolean restoreRegionBlocks(String fileName);

    /**
     * Restore all blocks in a region for restoring later.
     *
     * @param fileName The name of the file to save to (extension and folder will be added)
     * @return true if the region has been restored properly, otherwise false
     */
    CompletableFuture<Boolean> restoreRegionBlocksAsync(String fileName);

    /**
     * Reset all flags of the region.
     */
    void resetRegionFlags();

    /**
     * Indicate this region needs to be saved, saving will happen by a repeating task.
     */
    void saveRequired();

    /**
     * Check if a save is required.
     *
     * @return true if a save is required because some data changed, otherwise false
     */
    boolean isSaveRequired();

    /**
     * Save this region to disk now, using this method could slow down the plugin, normally saveRequired() should be used.
     *
     * @return true if the region is saved successfully, otherwise false
     */
    boolean saveNow();

    CompletableFuture<Boolean> saveNowAsync();

    /**
     * Check if the player can buy/rent this region, detailed info in the result object.
     *
     * @param type   The type of region to check
     * @param player The player to check it for
     * @return LimitResult containing if it is allowed, why and limiting factor
     */
    LimitResult limitsAllow(RegionType type, OfflinePlayer player);

    /**
     * Check if the player can buy/rent this region, detailed info in the result object.
     *
     * @param type          The type of region to check
     * @param offlinePlayer The player to check it for
     * @param extend        Check for extending of rental regions
     * @return LimitResult containing if it is allowed, why and limiting factor
     */
    LimitResult limitsAllow(RegionType type, OfflinePlayer offlinePlayer, boolean extend);

    /**
     * Check if this region matches the filters of a limit group.
     *
     * @param group The group to check
     * @return true if the region applies to the limit group, otherwise false
     */
    boolean matchesLimitGroup(String group);

    /**
     * Checks an event and handles saving to and restoring from schematic for it.
     *
     * @param type The type of event
     */
    void handleSchematicEvent(RegionEvent type);

    /**
     * Run commands as the CommandsSender, replacing all tags with the relevant values.
     *
     * @param sender   The sender that should perform the command
     * @param commands A list of the commands to run (without slash and with tags)
     */
    void runCommands(CommandSender sender, List<String> commands);

    /**
     * Get the volume of the region (number of blocks inside it).
     *
     * @return Number of blocks in the region
     */
    long volume();
}
