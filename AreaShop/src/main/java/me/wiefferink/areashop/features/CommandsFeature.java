package me.wiefferink.areashop.features;

import me.wiefferink.areashop.events.ask.AddingRegionEvent;
import me.wiefferink.areashop.events.ask.BuyingRegionEvent;
import me.wiefferink.areashop.events.ask.DeletingRegionEvent;
import me.wiefferink.areashop.events.ask.RentingRegionEvent;
import me.wiefferink.areashop.events.ask.ResellingRegionEvent;
import me.wiefferink.areashop.events.ask.SellingRegionEvent;
import me.wiefferink.areashop.events.ask.UnrentingRegionEvent;
import me.wiefferink.areashop.events.notify.AddedRegionEvent;
import me.wiefferink.areashop.events.notify.BoughtRegionEvent;
import me.wiefferink.areashop.events.notify.DeletedRegionEvent;
import me.wiefferink.areashop.events.notify.RentedRegionEvent;
import me.wiefferink.areashop.events.notify.ResoldRegionEvent;
import me.wiefferink.areashop.events.notify.SoldRegionEvent;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;
import me.wiefferink.areashop.regions.LegacyGeneralRegion;
import me.wiefferink.areashop.regions.util.RegionEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

public class CommandsFeature extends RegionFeature {

	/**
	 * Run command for a certain event.
	 * @param region Region to execute the events for
	 * @param event The event
	 * @param before The 'before' or 'after' commands
	 */
	public void runEventCommands(LegacyGeneralRegion region, RegionEvent event, boolean before) {
		ConfigurationSection eventCommandProfileSection = region.getConfigurationSectionSetting("general.eventCommandProfile", "eventCommandProfiles");
		if(eventCommandProfileSection == null) {
			return;
		}
		List<String> commands = eventCommandProfileSection.getStringList(event.getValue() + "." + (before ? "before" : "after"));
		if(commands.isEmpty()) {
			return;
		}
		region.runCommands(Bukkit.getConsoleSender(), commands);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void addingRegion(AddingRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.CREATED, true);
	}

	@EventHandler
	public void addedRegion(AddedRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.CREATED, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void deletingRegion(DeletingRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.DELETED, true);
	}

	@EventHandler
	public void deletedRegion(DeletedRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.DELETED, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void rentingRegion(RentingRegionEvent event) {
		// Technically the rent can still be cancelled if the payment fails...
		runEventCommands(event.getRegion(), event.isExtending() ? RegionEvent.EXTENDED : RegionEvent.RENTED, true);
	}

	@EventHandler
	public void rentedRegion(RentedRegionEvent event) {
		runEventCommands(event.getRegion(), event.hasExtended() ? RegionEvent.EXTENDED : RegionEvent.RENTED, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void buyingRegion(BuyingRegionEvent event) {
		// Technically the buy can still be cancelled if the payment fails...
		runEventCommands(event.getRegion(), RegionEvent.BOUGHT, true);
	}

	@EventHandler
	public void boughtRegion(BoughtRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.BOUGHT, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void resellingRegion(ResellingRegionEvent event) {
		// Technically the resell can still be cancelled if the payment fails...
		runEventCommands(event.getRegion(), RegionEvent.RESELL, true);
	}

	@EventHandler
	public void resoldRegion(ResoldRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.RESELL, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void sellingRegion(SellingRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.SOLD, true);
	}

	@EventHandler
	public void soldRegion(SoldRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.SOLD, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void unrentingRegion(UnrentingRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.UNRENTED, true);
	}

	@EventHandler
	public void unrentedRegion(UnrentedRegionEvent event) {
		runEventCommands(event.getRegion(), RegionEvent.UNRENTED, false);
	}

}
