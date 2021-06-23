package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Materials {

	private Materials() {
	}

	/**
	 * Get material based on a sign material name.
	 * @param name Name of the sign material
	 * @return null if not a sign, otherwise the material matching the name (when the material is not available on the current minecraft version, it returns the base type)
	 */
	public static Material signNameToMaterial(String name) {
		// Expected null case
		if (!isSign(name)) {
			return null;
		}
		try {
			return Material.matchMaterial(name);
		} catch (IllegalArgumentException ignored) {
			AreaShop.debug("Materials.get() null result:", name);
			return null;
		}
	}

	/**
	 * Check if a Material is a sign (of either the wall or floor type).
	 * @param material Material to check
	 * @return true if the given material is a sign
	 */
	public static boolean isSign(Material material) {
		return Tag.SIGNS.isTagged(material) || Tag.WALL_SIGNS.isTagged(material);
	}

	/**
	 * Check if a Material is a sign (of either the wall or floor type).
	 * @param name String to check
	 * @return true if the given material is a sign
	 */
	public static boolean isSign(String name) {
		try {
			return isSign(Material.matchMaterial(name));
		} catch (IllegalArgumentException ex) {
			AreaShop.debug("Invalid Material: " + name + " passed when performing sign check.");
			return false;
		}
	}

}
