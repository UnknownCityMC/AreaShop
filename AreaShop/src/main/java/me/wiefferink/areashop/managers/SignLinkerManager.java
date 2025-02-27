package me.wiefferink.areashop.managers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.MessageBridge;
import me.wiefferink.areashop.features.signs.RegionSign;
import me.wiefferink.areashop.features.signs.SignManager;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Materials;
import me.wiefferink.areashop.tools.SignUtils;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
public class SignLinkerManager extends Manager implements Listener {
	private final Map<UUID, SignLinker> signLinkers;
	private boolean eventsRegistered;

	@Inject
	private AreaShop plugin;
	@Inject
	private SignManager signManager;
	@Inject
	private MessageBridge messageBridge;

	SignLinkerManager() {
		signLinkers = new HashMap<>();
		eventsRegistered = false;
	}

	@Override
	public void shutdown() {
		for(UUID uuid : signLinkers.keySet()) {
			exitSignLinkMode(Bukkit.getPlayer(uuid));
		}
	}

	/**
	 * Let a player enter sign linking mode.
	 * @param player  The player that has to enter sign linking mode
	 * @param profile The profile to use for the signs (null for default)
	 */
	public void enterSignLinkMode(Player player, String profile) {
		signLinkers.put(player.getUniqueId(), new SignLinker(player, profile));
		messageBridge.message(player, "linksigns-first");
		messageBridge.message(player, "linksigns-next");
		if(!eventsRegistered) {
			eventsRegistered = true;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	/**
	 * Let a player exit sign linking mode.
	 * @param player The player that has to exit sign linking mode
	 */
	public void exitSignLinkMode(Player player) {
		signLinkers.remove(player.getUniqueId());
		if(eventsRegistered && signLinkers.isEmpty()) {
			eventsRegistered = false;
			HandlerList.unregisterAll(this);
		}
		messageBridge.message(player, "linksigns-stopped");
	}

	/**
	 * Check if the player is in sign linking mode.
	 * @param player The player to check
	 * @return true if the player is in sign linking mode, otherwise false
	 */
	public boolean isInSignLinkMode(Player player) {
		return signLinkers.containsKey(player.getUniqueId());
	}

	/**
	 * On player interactions.
	 * @param event The PlayerInteractEvent
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(isInSignLinkMode(event.getPlayer())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			SignLinker linker = signLinkers.get(event.getPlayer().getUniqueId());
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// Get the region
				BlockIterator blockIterator = new BlockIterator(player, 100);
				while(blockIterator.hasNext()) {
					Block next = blockIterator.next();
					List<GeneralRegion> regions = Utils.getRegions(next.getLocation());
					if(regions.size() == 1) {
						linker.setRegion(regions.getFirst());
						return;
					} else if(regions.size() > 1) {
						Set<String> names = new HashSet<>();
						for(GeneralRegion region : regions) {
							names.add(region.getName());
						}
						messageBridge.message(player, "linksigns-multipleRegions", Utils.createCommaSeparatedList(names));
						messageBridge.message(player, "linksigns-multipleRegionsAdvice");
						return;
					}
				}
				// No regions found within the maximum range
				messageBridge.message(player, "linksigns-noRegions");
			} else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				Block block = null;
				BlockIterator blockIterator = new BlockIterator(player, 100);
				while(blockIterator.hasNext() && block == null) {
					Block next = blockIterator.next();
					if(next.getType() != Material.AIR) {
						block = next;
					}
				}
				if(block == null || !Materials.isSign(block.getType())) {
					messageBridge.message(player, "linksigns-noSign");
					return;
				}

				Optional<RegionSign> optional = signManager.signFromLocation(block.getLocation());
				if(optional.isPresent()) {
					RegionSign regionSign = optional.get();
					messageBridge.message(player, "linksigns-alreadyRegistered", regionSign.getRegion());
					return;
				}
				linker.setSign(block.getLocation(), block.getType(), SignUtils.getSignFacing(block));
			}
		}
	}

	/**
	 * Handle disconnection players.
	 * @param event The PlayerQuitEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent event) {
		exitSignLinkMode(event.getPlayer());
	}

	/**
	 * Class to keep track of the signlinking data.
	 */
	private class SignLinker {
		private boolean hasSign = false;
		private boolean hasRegion = false;

		public final Player linker;
		public final String profile;

		public GeneralRegion region = null;

		public Location location = null;
		public Material type = null;
		public BlockFace facing = null;

		public SignLinker(Player linker, String profile) {
			this.linker = linker;
			this.profile = profile;
		}

		public void setRegion(GeneralRegion region) {
			this.region = region;
			hasRegion = true;
			if(!isComplete()) {
				messageBridge.message(linker, "linksigns-regionFound", region);
			}
			finish();
		}

		public void setSign(Location location, Material type, BlockFace facing) {
			this.location = location;
			this.type = type;
			this.facing = facing;
			hasSign = true;
			if(!isComplete()) {
				messageBridge.message(linker, "linksigns-signFound", location.getBlockX(), location.getBlockY(), location.getBlockZ());
			}
			finish();
		}

		public void finish() {
			if(isComplete()) {
				region.getSignsFeature().addSign(location, type, facing, profile);
				if(profile == null) {
					messageBridge.message(linker, "addsign-success", region);
				} else {
					messageBridge.message(linker, "addsign-successProfile", region, profile);
				}
				region.update();
				reset();

				messageBridge.message(linker, "linksigns-next");
			}
		}

		public void reset() {
			hasSign = false;
			hasRegion = false;
		}

		public boolean isComplete() {
			return hasSign && hasRegion;
		}
	}


}
















