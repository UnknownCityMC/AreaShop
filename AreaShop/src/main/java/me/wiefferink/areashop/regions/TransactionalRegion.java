package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

public abstract class TransactionalRegion extends GeneralRegion {

    private final String key;

    private final RegionType type;
    private UUID transactionHolder;
    private String transactionHolderName;
    private double price;
    private double moneyBackPercentage;
    private long inactivityThresholdMillis;

    public TransactionalRegion(YamlConfiguration config, RegionType regionType) {
        super(config);
        this.type = regionType;
        this.key = regionType.name().toLowerCase() + ".";
        this.moneyBackPercentage = config.getDouble(this.key + "moneyBack");
    }

    public TransactionalRegion(String name, World world, RegionType type) {
        super(name, world);
        this.type = type;
        this.key = type.name().toLowerCase() + ".";
    }

    protected final void setInactivityThresholdMillis(long inactivityThresholdMillis) {
        this.inactivityThresholdMillis = inactivityThresholdMillis;
    }

    @Override
    public RegionType getType() {
        return type;
    }

    @Override
    public boolean isAvailable() {
        return !isSold();
    }

    /**
     * Get the UUID of the owner of this region.
     *
     * @return The UUID of the owner of this region
     */
    public UUID getTransactionHolder() {
        return transactionHolder;
    }

    /**
     * Set the buyer of this region. WARNING - May trigger disk lookup for offline players!
     *
     * @param transactionHolder The UUID of the player that should be set as buyer
     */
    public void setTransactionHolder(UUID transactionHolder) {
        if (transactionHolder == null) {
            this.transactionHolder = null;
            this.transactionHolderName = null;
        } else {
            this.transactionHolder = transactionHolder;
            String name = Utils.toName(transactionHolder);
            this.transactionHolderName = name.isEmpty() ? null : name;
        }
    }

    /**
     * Set the buyer of this region.
     *
     * @param player The instance of the player.
     */
    public void setTransactionHolder(final OfflinePlayer player) {
        if (player == null) {
            this.transactionHolder = null;
            this.transactionHolderName = null;
        } else {
            String name = player.getName();
            this.transactionHolderName = name == null || name.isEmpty() ? null : name;
        }
    }

    @Override
    public void setOwner(UUID player) {
        setTransactionHolder(player);
    }

    /**
     * Check if a player is the buyer/holder of this region.
     *
     * @param player Player to check
     * @return true if this player owns this region, otherwise false
     */
    public boolean isTransactionHolder(OfflinePlayer player) {
        return player != null && player.getUniqueId().equals(transactionHolder);
    }

    public boolean isTransactionHolder(UUID player) {
        UUID buyer = getTransactionHolder();
        return !(buyer == null || player == null) && buyer.equals(player);
    }

    /**
     * Get the name of the player that owns this region.
     *
     * @return The name of the player that owns this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return &lt;UNKNOWN&gt;
     */
    public String getPlayerName() {
        return this.transactionHolderName == null ? "<UNKNOWN>" : transactionHolderName;
    }

    /**
     * Check if the region is sold.
     *
     * @return true if the region is sold, otherwise false
     */
    public boolean isSold() {
        return this.transactionHolder != null;
    }

    /**
     * Get the price of the region.
     *
     * @return The price of the region
     */
    public double getPrice() {
        return price;
    }

    /**
     * Change the price of the region.
     *
     * @param price The price to set this region to
     */
    public void setPrice(double price) {
        this.price = price;
        super.config.set(key + "price", price);
    }

    /**
     * Get the formatted string of the price (includes prefix and suffix).
     *
     * @return The formatted string of the price
     */
    public String getFormattedPrice() {
        return Utils.formatCurrency(getPrice());
    }

    /**
     * Get the moneyBack percentage.
     *
     * @return The % of money the player will get back when selling
     */
    public double getMoneyBackPercentage() {
        return moneyBackPercentage;
    }

    /**
     * Get the amount of money that should be paid to the player when selling the region.
     *
     * @return The amount of money the player should get back
     */
    public double getMoneyBackAmount() {
        return getPrice() * (moneyBackPercentage / 100D);
    }

    /**
     * Get the formatted string of the amount of the moneyBack amount.
     *
     * @return String with currency symbols and proper fractional part
     */
    public String getFormattedMoneyBackAmount() {
        return Utils.formatCurrency(getMoneyBackAmount());
    }

    @Override
    public Object provideReplacement(String variable) {
        switch (variable) {
            case AreaShop.tagPrice:
                return getFormattedPrice();
            case AreaShop.tagRawPrice:
                return getPrice();
            case AreaShop.tagPlayerName:
                return getPlayerName();
            case AreaShop.tagPlayerUUID:
                return getTransactionHolder();
            case AreaShop.tagMoneyBackAmount:
                return getFormattedMoneyBackAmount();
            case AreaShop.tagRawMoneyBackAmount:
                return getMoneyBackAmount();
            case AreaShop.tagMoneyBackPercentage:
                return getMoneyBackPercentage() % 1.0 == 0.0 ? (int) getMoneyBackPercentage() : getMoneyBackPercentage();
            case AreaShop.tagMaxInactiveTime:
                return this.getFormattedInactivityThreshold();
            default:
                return super.provideReplacement(variable);
        }
    }

    public long getInactivityThresholdMillis() {
        return this.inactivityThresholdMillis;
    }

    /**
     * Get a human readable string indicating how long the player can be offline until automatic unrent.
     *
     * @return String indicating the inactive time until unrent
     */
    public String getFormattedInactivityThreshold() {
        return Utils.millisToHumanFormat(this.inactivityThresholdMillis);
    }

    @Override
    public boolean checkInactivity(final OfflinePlayer player) {
        if (isDeleted() || !isSold()) {
            return false;
        }
        long inactiveSetting = getInactivityThresholdMillis();
        if (inactiveSetting <= 0) {
            return false;
        }
        if (player.isOnline()) {
            return false;
        }
        long lastPlayed = getLastActiveTime();
        long now = System.currentTimeMillis();
        if (now > (lastPlayed + inactiveSetting)) {
            AreaShop.info("Region " + getName() + " unrented because of inactivity for player " + getPlayerName());
            AreaShop.debug("currentTime=" + now + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (now - player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
            return true;
        }
        return false;
    }
}
