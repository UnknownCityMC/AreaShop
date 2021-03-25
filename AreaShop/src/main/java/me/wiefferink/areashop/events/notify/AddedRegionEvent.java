package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;

/**
 * Broadcasted when a region has been added to AreaShop.
 */
public class AddedRegionEvent extends NotifyRegionEvent<LegacyGeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been added
	 */
	public AddedRegionEvent(LegacyGeneralRegion region) {
		super(region);
	}

}
