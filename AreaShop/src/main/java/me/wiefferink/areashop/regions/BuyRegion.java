package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.ask.BuyingRegionEvent;
import me.wiefferink.areashop.events.ask.ResellingRegionEvent;
import me.wiefferink.areashop.events.ask.SellingRegionEvent;
import me.wiefferink.areashop.events.notify.BoughtRegionEvent;
import me.wiefferink.areashop.events.notify.ResoldRegionEvent;
import me.wiefferink.areashop.events.notify.SoldRegionEvent;
import me.wiefferink.areashop.tools.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BuyRegion extends TransactionalRegion {

    private static final String UNKNOWN_BUYER = "<UNKNOWN>";

    public BuyRegion(YamlConfiguration config) {
        super(config, RegionType.BUY);
    }

    public BuyRegion(String name, World world) {
        super(name, world, RegionType.BUY);
    }

    @Override
    public RegionState getState() {
        if (isSold() && isInResellingMode()) {
            return RegionState.RESELL;
        } else if (isSold() && !isInResellingMode()) {
            return RegionState.SOLD;
        } else {
            return RegionState.FORSALE;
        }
    }

    /**
     * Get the UUID of the owner of this region.
     *
     * @return The UUID of the owner of this region
     */
    @Deprecated
    public UUID getBuyer() {
        String buyer = config.getString("buy.buyer");
        if (buyer != null) {
            try {
                return UUID.fromString(buyer);
            } catch (IllegalArgumentException e) {
                // Incorrect UUID
            }
        }
        return null;
    }

    /**
     * Set the buyer of this region.
     *
     * @param buyer The UUID of the player that should be set as buyer
     */
    @Deprecated
    public void setBuyer(UUID buyer) {
        if (buyer == null) {
            setSetting("buy.buyer", null);
            setSetting("buy.buyerName", null);
        } else {
            setSetting("buy.buyer", buyer.toString());
            setSetting("buy.buyerName", Utils.toName(buyer));
        }
        this.setTransactionHolder(buyer);
    }

    /**
     * Check if a player is the buyer of this region.
     *
     * @param player Player to check
     * @return true if this player owns this region, otherwise false
     */
    @Deprecated
    public boolean isBuyer(OfflinePlayer player) {
        return player != null && isBuyer(player.getUniqueId());
    }

    @Deprecated
    public boolean isBuyer(UUID player) {
        UUID buyer = getBuyer();
        return !(buyer == null || player == null) && buyer.equals(player);
    }

    /**
     * Get the name of the player that owns this region.
     *
     * @return The name of the player that owns this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return &lt;UNKNOWN&gt;
     */
    public String getPlayerName() {
        String result = Utils.toName(getBuyer());
        if (result.isEmpty()) {
            result = getStringSetting("buy.buyerName");
            if (result == null || result.isEmpty()) {
                result = UNKNOWN_BUYER;
            }
        }
        return result;
    }

    /**
     * Check if the region is being resold.
     *
     * @return true if the region is available for reselling, otherwise false
     */
    public boolean isInResellingMode() {
        return config.getBoolean("buy.resellMode");
    }

    /**
     * Get the resell price of this region.
     *
     * @return The resell price if isInResellingMode(), otherwise 0.0
     */
    public double getResellPrice() {
        return Math.max(0, config.getDouble("buy.resellPrice"));
    }

    /**
     * Get the formatted string of the resellprice (includes prefix and suffix).
     *
     * @return The formatted string of the resellprice
     */
    public String getFormattedResellPrice() {
        return Utils.formatCurrency(getResellPrice());
    }

    /**
     * Set the region into resell mode with the given price.
     *
     * @param price The price this region should be put up for sale
     */
    public void enableReselling(double price) {
        setSetting("buy.resellMode", true);
        setSetting("buy.resellPrice", price);
    }

    /**
     * Stop this region from being in resell mode.
     */
    public void disableReselling() {
        setSetting("buy.resellMode", null);
        setSetting("buy.resellPrice", null);
    }

    @Override
    public Object provideReplacement(String variable) {
        switch (variable) {
            case AreaShop.tagResellPrice:
                return getFormattedResellPrice();
            case AreaShop.tagRawResellPrice:
                return getResellPrice();
            default:
                return super.provideReplacement(variable);
        }
    }

    /**
     * Minutes until automatic unrent when player is offline.
     *
     * @return The number of milliseconds until the region is unrented while player is offline
     */
    @Deprecated
    public long getInactiveTimeUntilSell() {
        return Utils.getDurationFromMinutesOrStringInput(getStringSetting("buy.inactiveTimeUntilSell"));
    }

    /**
     * Get a human readable string indicating how long the player can be offline until automatic unrent.
     *
     * @return String indicating the inactive time until unrent
     */
    @Deprecated
    public String getFormattedInactiveTimeUntilSell() {
        return Utils.millisToHumanFormat(getInactiveTimeUntilSell());
    }

    /**
     * Buy a region.
     *
     * @param offlinePlayer The player that wants to buy the region
     * @return true if it succeeded and false if not
     */
    @SuppressWarnings("deprecation")
    public boolean buy(OfflinePlayer offlinePlayer) {
        // Check if the player has permission
        if (!plugin.hasPermission(offlinePlayer, "areashop.buy")) {
            message(offlinePlayer, "buy-noPermission");
            return false;
        }

        if (plugin.getEconomy() == null) {
            message(offlinePlayer, "general-noEconomy");
            return false;
        }

        if (isInResellingMode()) {
            if (!plugin.hasPermission(offlinePlayer, "areashop.buyresell")) {
                message(offlinePlayer, "buy-noPermissionResell");
                return false;
            }
        } else {
            if (!plugin.hasPermission(offlinePlayer, "areashop.buynormal")) {
                message(offlinePlayer, "buy-noPermissionNoResell");
                return false;
            }
        }

        if (getWorld() == null) {
            message(offlinePlayer, "general-noWorld");
            return false;
        }

        if (getRegion() == null) {
            message(offlinePlayer, "general-noRegion");
            return false;
        }

        if (isSold() && !(isInResellingMode() && !isBuyer(offlinePlayer))) {
            if (isBuyer(offlinePlayer)) {
                message(offlinePlayer, "buy-yours");
            } else {
                message(offlinePlayer, "buy-someoneElse");
            }
            return false;
        }

        boolean isResell = isInResellingMode();

        // Only relevant if the player is online
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            // Check if the players needs to be in the region for buying
            if (restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName())
                    || !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
                message(offlinePlayer, "buy-restrictedToRegion");
                return false;
            }
            // Check if the players needs to be in the world for buying
            if (restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
                message(offlinePlayer, "buy-restrictedToWorld", player.getWorld().getName());
                return false;
            }
        }

        // Check region limits
        LimitResult limitResult = this.limitsAllow(RegionType.BUY, offlinePlayer);
        AreaShop.debug("LimitResult: " + limitResult.toString());
        if (!limitResult.actionAllowed()) {
            if (limitResult.getLimitingFactor() == LimitType.TOTAL) {
                message(offlinePlayer, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
                return false;
            }
            if (limitResult.getLimitingFactor() == LimitType.BUYS) {
                message(offlinePlayer, "buy-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
                return false;
            }
            // Should not be reached, but is safe like this
            return false;
        }

        // Check if the player has enough money
        if (isResell && !plugin.getEconomy().has(offlinePlayer, getWorldName(), getResellPrice())) {
            message(offlinePlayer, "buy-lowMoneyResell", Utils.formatCurrency(plugin.getEconomy().getBalance(offlinePlayer, getWorldName())));
            return false;
        }
        if (!isResell && !plugin.getEconomy().has(offlinePlayer, getWorldName(), getPrice())) {
            message(offlinePlayer, "buy-lowMoney", Utils.formatCurrency(plugin.getEconomy().getBalance(offlinePlayer, getWorldName())));
            return false;
        }

        UUID oldOwner = getBuyer();
        if (isResell && oldOwner != null) {
            // Broadcast and check event
            ResellingRegionEvent event = new ResellingRegionEvent(this, offlinePlayer);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                message(offlinePlayer, "general-cancelled", event.getReason());
                return false;
            }

            getFriendsFeature().clearFriends();
            double resellPrice = getResellPrice();
            // Transfer the money to the previous owner
            EconomyResponse r = plugin.getEconomy().withdrawPlayer(offlinePlayer, getWorldName(), getResellPrice());
            if (!r.transactionSuccess()) {
                message(offlinePlayer, "buy-payError");
                AreaShop.debug("Something went wrong with getting money from " + offlinePlayer.getName() + " while buying " + getName() + ": " + r.errorMessage);
                return false;
            }
            r = null;
            OfflinePlayer oldOwnerPlayer = Bukkit.getOfflinePlayer(oldOwner);
            String oldOwnerName = getPlayerName();
            if (oldOwnerPlayer.hasPlayedBefore() && oldOwnerPlayer.getName() != null) {
                r = plugin.getEconomy().depositPlayer(oldOwnerPlayer, getWorldName(), getResellPrice());
                oldOwnerName = oldOwnerPlayer.getName();
            } else if (oldOwnerName != null) {
                r = plugin.getEconomy().depositPlayer(oldOwnerName, getWorldName(), getResellPrice());
            }
            if (r == null || !r.transactionSuccess()) {
                AreaShop.warn("Something went wrong with paying '" + oldOwnerName + "' " + getFormattedPrice() + " for his resell of region " + getName() + " to " + offlinePlayer.getName());
            }
            // Resell is done, disable that now
            disableReselling();

            // Set the owner
            setBuyer(offlinePlayer.getUniqueId());
            updateLastActiveTime();

            // Update everything
            handleSchematicEvent(RegionEvent.RESELL);

            // Notify about updates
            this.notifyAndUpdate(new ResoldRegionEvent(this, oldOwner));

            // Send message to the player
            message(offlinePlayer, "buy-successResale", oldOwnerName);
            Player seller = Bukkit.getPlayer(oldOwner);
            if (seller != null) {
                message(seller, "buy-successSeller", resellPrice);
            }
        } else {
            // Broadcast and check event
            BuyingRegionEvent event = new BuyingRegionEvent(this, offlinePlayer);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                message(offlinePlayer, "general-cancelled", event.getReason());
                return false;
            }

            // Substract the money from the players balance
            EconomyResponse r = plugin.getEconomy().withdrawPlayer(offlinePlayer, getWorldName(), getPrice());
            if (!r.transactionSuccess()) {
                message(offlinePlayer, "buy-payError");
                return false;
            }
            // Optionally give money to the landlord
            OfflinePlayer landlordPlayer = null;
            if (getLandlord() != null) {
                landlordPlayer = Bukkit.getOfflinePlayer(getLandlord());
            }
            String landlordName = getLandlordName();
            if (landlordName != null) {
                if (landlordPlayer != null && landlordPlayer.getName() != null) {
                    r = plugin.getEconomy().depositPlayer(landlordPlayer, getWorldName(), getPrice());
                } else {
                    r = plugin.getEconomy().depositPlayer(landlordName, getWorldName(), getPrice());
                }
                if (r != null && !r.transactionSuccess()) {
                    AreaShop.warn("Something went wrong with paying '" + landlordName + "' " + getFormattedPrice() + " for his sell of region " + getName() + " to " + offlinePlayer.getName());
                }
            }

            // Set the owner
            setBuyer(offlinePlayer.getUniqueId());
            updateLastActiveTime();

            // Send message to the player
            message(offlinePlayer, "buy-succes");

            // Update everything
            handleSchematicEvent(RegionEvent.BOUGHT);

            // Notify about updates
            this.notifyAndUpdate(new BoughtRegionEvent(this));
        }
        return true;
    }

    /**
     * Sell a purchased region, get part of the money back.
     *
     * @param giveMoneyBack true if the player should be given money back, otherwise false
     * @param executor      CommandSender to receive a message when the sell fails, or null
     * @return true if the region has been sold, otherwise false
     */
    @SuppressWarnings("deprecation")
    public boolean sell(boolean giveMoneyBack, CommandSender executor) {
        boolean own = executor instanceof Player && this.isBuyer((Player) executor);
        if (executor != null) {
            if (!executor.hasPermission("areashop.sell") && !own) {
                message(executor, "sell-noPermissionOther");
                return false;
            }
            if (!executor.hasPermission("areashop.sell") && !executor.hasPermission("areashop.sellown") && own) {
                message(executor, "sell-noPermission");
                return false;
            }
            if (!executor.hasPermission("areashop.sell")
                    && executor.hasPermission("areashop.sellown")
                    && own
                    && getBooleanSetting("buy.sellDisabled")) {
                message(executor, "sell-disabled");
                return false;
            }
        }

        if (plugin.getEconomy() == null) {
            return false;
        }

        // Broadcast and check event
        SellingRegionEvent event = new SellingRegionEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            message(executor, "general-cancelled", event.getReason());
            return false;
        }

        disableReselling();
        // Give part of the buying price back
        double moneyBack = getMoneyBackAmount();
        if (moneyBack > 0 && giveMoneyBack) {
            boolean noPayBack = false;
            OfflinePlayer landlordPlayer = null;
            if (getLandlord() != null) {
                landlordPlayer = Bukkit.getOfflinePlayer(getLandlord());
            }
            String landlordName = getLandlordName();
            EconomyResponse r;
            if (landlordName != null) {
                if (landlordPlayer != null && landlordPlayer.getName() != null) {
                    r = plugin.getEconomy().withdrawPlayer(landlordPlayer, getWorldName(), moneyBack);
                } else {
                    r = plugin.getEconomy().withdrawPlayer(landlordName, getWorldName(), moneyBack);
                }
                if (r == null || !r.transactionSuccess()) {
                    noPayBack = true;
                }
            }

            // Give back the money
            OfflinePlayer player = Bukkit.getOfflinePlayer(getTransactionHolder());
            if (player.hasPlayedBefore() && !noPayBack) {
                EconomyResponse response = null;
                boolean error = false;
                try {
                    if (player.getName() != null) {
                        response = plugin.getEconomy().depositPlayer(player, getWorldName(), moneyBack);
                    } else if (getPlayerName() != null) {
                        response = plugin.getEconomy().depositPlayer(getPlayerName(), getWorldName(), moneyBack);
                    }
                } catch (Exception e) {
                    error = true;
                }
                if (error || response == null || !response.transactionSuccess()) {
                    AreaShop.warn("Something went wrong with paying back money to " + getPlayerName() + " while selling region " + getName());
                }
            }
        }

        // Handle schematic save/restore (while %uuid% is still available)
        handleSchematicEvent(RegionEvent.SOLD);

        // Send message: before actual removal of the buyer so that it is still available for variables
        message(executor, "sell-sold");

        // Remove friends and the owner
        getFriendsFeature().clearFriends();
        UUID oldBuyer = getBuyer();
        setBuyer(null);
        removeLastActiveTime();

        // Notify about updates
        this.notifyAndUpdate(new SoldRegionEvent(this, oldBuyer, Math.max(moneyBack, 0)));
        return true;
    }

    @Override
    public boolean handleInactivity() {
        return this.sell(true, null);
    }
}

























