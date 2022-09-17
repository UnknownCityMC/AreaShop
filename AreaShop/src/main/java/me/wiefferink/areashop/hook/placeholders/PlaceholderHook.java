package me.wiefferink.areashop.hook.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.managers.Manager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;

public class PlaceholderHook extends Manager {

    private boolean enabled;

    private final Collection<PlaceholderExpansion> expansions = new ArrayList<>();

    public final boolean enabled() {
        return enabled;
    }

    public boolean tryEnable(AreaShop plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return false;
        }
        if (enabled) {
            return true;
        }
        plugin.getLogger().info(() -> "Detected PlaceHolderAPI; registering placeholders");
        PlaceholderExpansion generalRegionExp = new BuiltinRegionExpansion(plugin.getFileManager());
        plugin.debugI(() -> "Initialized the GeneralRegionExpansion");
        expansions.add(generalRegionExp);
        expansions.forEach(PlaceholderExpansion::register);
        enabled = true;
        return true;
    }

    @Override
    public void shutdown() {
        if (!enabled) {
            return;
        }
        expansions.forEach(PlaceholderExpansion::unregister);
        expansions.clear();
        enabled = false;
    }

}
