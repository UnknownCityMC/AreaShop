package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;

/**
 * Broadcasted when a region has been removed from AreaShop.
 */
public class DeletedRegionEvent extends NotifyRegionEvent<LegacyGeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been removed
	 */
	public DeletedRegionEvent(LegacyGeneralRegion region) {
		super(region);
	}
}
