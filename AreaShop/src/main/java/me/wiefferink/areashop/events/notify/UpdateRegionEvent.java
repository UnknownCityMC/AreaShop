package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;
import me.wiefferink.areashop.regions.MarketableRegion;

/**
 * Broadcasted when the data of a region changes.
 * Should be used for updating displays that use region data.
 */
public class UpdateRegionEvent extends NotifyRegionEvent<MarketableRegion> {

	/**
	 * Contructor.
	 * @param region The region that has been updated
	 */
	public UpdateRegionEvent(MarketableRegion region) {
		super(region);
	}
}
