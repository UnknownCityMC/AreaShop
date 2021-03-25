package me.wiefferink.areashop.interfaces;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public interface IRegion {

	ProtectedRegion getRegion();

	String getRegionId();

	World getWorld();

	String getWorldName();

	int getWidth();

	int getDepth();

	int getHeight();

}
