package me.wiefferink.areashop.regions.util;

/**
 * Class to store the result of a limits check.
 */
public class LimitResult {
    private final boolean actionAllowed;
    private final LimitType limitingFactor;
    private final int maximum;
    private final int current;
    private final String limitingGroup;

    /**
     * Constructor.
     *
     * @param actionAllowed  has the action been allowed?
     * @param limitingFactor The LimitType that has prevented the action (if actionAllowed is false)
     * @param maximum        The maximum number of regions allowed (if actionAllowed is false)
     * @param current        The current number of regions the player has (if actionAllowed is false)
     * @param limitingGroup  The group that is enforcing this limit (if actionAllowed is false)
     */
    public LimitResult(boolean actionAllowed, LimitType limitingFactor, int maximum, int current, String limitingGroup) {
        this.actionAllowed = actionAllowed;
        this.limitingFactor = limitingFactor;
        this.maximum = maximum;
        this.current = current;
        this.limitingGroup = limitingGroup;
    }

    /**
     * Check if the action is allowed.
     *
     * @return true if the actions is allowed, otherwise false
     */
    public boolean actionAllowed() {
        return actionAllowed;
    }

    /**
     * Get the type of the factor that is limiting the action, assuming actionAllowed() is false.
     *
     * @return The type of the limiting factor
     */
    public LimitType getLimitingFactor() {
        return limitingFactor;
    }

    /**
     * Get the maximum number of the group that is the limiting factor, assuming actionAllowed() is false.
     *
     * @return The maximum
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * Get the current number of regions in the group that is the limiting factor, assuming actionAllowed() is false.
     *
     * @return The current number of regions the player has
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Get the name of the group that is limiting the action, assuming actionAllowed() is false.
     *
     * @return The name of the group
     */
    public String getLimitingGroup() {
        return limitingGroup;
    }

    @Override
    public String toString() {
        return "actionAllowed=" + actionAllowed + ", limitingFactor=" + limitingFactor + ", maximum=" + maximum + ", current=" + current + ", limitingGroup=" + limitingGroup;
    }
}
