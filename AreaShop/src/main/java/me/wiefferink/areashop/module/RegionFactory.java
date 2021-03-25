package me.wiefferink.areashop.module;

import me.wiefferink.areashop.interfaces.IRegion;
import me.wiefferink.areashop.regions.util.RegionFeatureHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReadWriteLock;

public interface RegionFactory {

    @NotNull RegionFeatureHelper createFeatureHelper(@NotNull IRegion region, @NotNull ReadWriteLock readWriteLock);

    @NotNull RegionFeatureHelper createFeatureHelper(@NotNull IRegion region);

}
