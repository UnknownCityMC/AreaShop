package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableRegionEvent;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;

/**
 * Broadcasted when a region has been added to AreaShop.
 */
public class AddingRegionEvent extends CancellableRegionEvent<LegacyGeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been added
	 */
	public AddingRegionEvent(LegacyGeneralRegion region) {
		super(region);
	}

}
