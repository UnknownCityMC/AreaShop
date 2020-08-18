package me.wiefferink.areashop.listeners;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.regions.TransactionalRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Notify region expiry and track activity time.
 */
public final class PlayerLoginLogoutListener implements Listener {
    private final AreaShop plugin;

    /**
     * Constructor.
     *
     * @param plugin The AreaShop plugin
     */
    public PlayerLoginLogoutListener(AreaShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a sign is changed.
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != Result.ALLOWED) {
            return;
        }
        final Player player = event.getPlayer();

        // Schedule task to check for notifications, prevents a lag spike at login
        Utils.runCancellingTaskTimer(() -> {
            // Delay until all regions are loaded
            if (!plugin.isReady()) {
                return true;
            }
            if (!player.isOnline()) {
                return false;
            }
            // Notify for rents that almost run out
            for (GeneralRegion generalRegion : plugin.getFileManager().getRegions()) {
                if (!(generalRegion instanceof RentRegion)) {
                    continue;
                }
                final RentRegion region = (RentRegion) generalRegion;
                if (region.isTransactionHolder(player)) {
                    String warningSetting = region.getStringSetting("rent.warningOnLoginTime");
                    if (warningSetting == null || warningSetting.isEmpty()) {
                        continue;
                    }
                    long warningTime = Utils.durationStringToLong(warningSetting);
                    if (region.getTimeLeft() < warningTime) {
                        // Send the warning message later to let it appear after general MOTD messages
                        AreaShop.getInstance().message(player, "rent-expireWarning", region);
                    }
                }
            }

            // Notify admins for plugin updates
            AreaShop.getInstance().notifyUpdate(player);
            return false;
        }, 25, 25, false);

        // Check if the player has regions that use an old name of him and update them
        Utils.runCancellingTaskTimer(() -> {
            if (!plugin.isReady()) {
                return true;
            }

            List<GeneralRegion> regions = new LinkedList<>();
            for (GeneralRegion region : plugin.getFileManager().getRegions()) {
                if (region instanceof TransactionalRegion && ((TransactionalRegion) region).isTransactionHolder(player)) {
                    regions.add(region);
                }
            }
            final String name = player.getName();
            Utils.runAsBatches(regions,
                    plugin.getConfig().getInt("nameupdate.regionsPerTick"),
                    (region) -> {
                        if (region instanceof BuyRegion || region instanceof RentRegion) {
                            String key = region instanceof BuyRegion ? "buy.buyerName" : "rent.renterName";
                            if (!name.equals(region.getStringSetting("buy.buyerName"))) {
                                region.setSetting(key, player.getName());
                                region.update();
                            }
                        }
                    }, false);
            return false;
        }, 22, 10, false);
    }

    // Active time updates
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event) {
        updateLastActive(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        updateLastActive(event.getPlayer());
    }

    /**
     * Update the last active time for all regions the player is owner off.
     *
     * @param player The player to update the active times for
     */
    private void updateLastActive(Player player) {
        for (GeneralRegion region : plugin.getFileManager().getRegions()) {
            if (region instanceof TransactionalRegion && ((TransactionalRegion) region).isTransactionHolder(player)) {
                region.updateLastActiveTime();
            }
        }
    }
}
































