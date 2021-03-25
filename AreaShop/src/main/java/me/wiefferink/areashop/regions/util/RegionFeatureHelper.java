package me.wiefferink.areashop.regions.util;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import me.wiefferink.areashop.features.RegionFeature;
import me.wiefferink.areashop.interfaces.IRegion;
import me.wiefferink.areashop.managers.FeatureManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RegionFeatureHelper {

    @Inject
    private FeatureManager featureManager;

    private final ReadWriteLock readWriteLock;
    private final IRegion region;

    private final ClassToInstanceMap<RegionFeature> features = MutableClassToInstanceMap.create();

    @AssistedInject
    public RegionFeatureHelper(@NotNull @Assisted IRegion region, @NotNull @Assisted ReadWriteLock readWriteLock) {
        this.region = region;
        this.readWriteLock = readWriteLock;
    }

    @AssistedInject
    public RegionFeatureHelper(@NotNull @Assisted IRegion region) {
        this(region, new ReentrantReadWriteLock());
    }

    public <T extends RegionFeature> @NotNull T feature(@NotNull Class<T> clazz) {
        this.readWriteLock.writeLock().lock();
        try {
            if (this.features.containsKey(clazz)) {
                return this.features.getInstance(clazz);
            }
            final T feature = featureManager.getRegionFeature(this.region, clazz);
            this.features.put(clazz, feature);
            return feature;
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    public @NotNull Collection<? extends RegionFeature> valuesCopy() {
        this.readWriteLock.readLock().lock();
        try {
            return new HashSet<>(this.features.values());
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    public @NotNull Collection<? extends RegionFeature> values() {
        this.readWriteLock.readLock().lock();
        try {
            return Collections.unmodifiableCollection(this.features.values());
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

}
