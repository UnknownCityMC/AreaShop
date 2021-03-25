package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.regions.util.RegionStatus;
import me.wiefferink.areashop.regions.util.RegionType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AbstractRegionMeta implements RegionMeta {

    private static final String KEY_REGION_ID = "name";
    private static final String KEY_RESTORE = "enableRestore";
    private static final String KEY_SCHEMATIC_PROFILE = "schematicProfile";
    //private static final String KEY_LAST_ACTIVE = "lastActive";
    private static final String KEY_WORLD_NAME = "world";
    private static final String KEY_LANDLORD = "landlord";
    private static final String KEY_LANDLORD_NAME = "landlordName";

    private static final String KEY_COUNT_FOR_LIMITS = "countForLimits";

    protected transient final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private @NotNull String regionId;
    private @NotNull String world;
    private @Nullable String schematicProfile;
    private @NotNull RegionStatus regionStatus;

    private @Nullable UUID landlord;
    private @Nullable String lastKnownLandlordName;

    private boolean restoreEnabled;
    private boolean restrictedToRegion;
    private boolean restrictedToWorld;
    private boolean countForLimits;

    protected AbstractRegionMeta(@NotNull ConfigurationSection configurationSection) {
        load0(configurationSection);
    }

    protected AbstractRegionMeta(@NotNull String regionId,
                                 @NotNull String world,
                                 @Nullable String schematicProfile,
                                 @NotNull RegionType regionType,
                                 @NotNull RegionStatus regionStatus,
                                 boolean restoreEnabled,
                                 boolean restrictedToRegion,
                                 boolean restrictedToWorld) {

        this.regionId = Objects.requireNonNull(regionId);
        this.world = Objects.requireNonNull(world);
        this.schematicProfile = Objects.requireNonNull(schematicProfile);
        this.regionStatus = Objects.requireNonNull(regionStatus);
        this.restoreEnabled = restoreEnabled;
        this.restrictedToRegion = restrictedToRegion;
        this.restrictedToWorld = restrictedToWorld;

    }

    private void load0(@NotNull ConfigurationSection section) {
        this.regionId = Objects.requireNonNull(section.getString(KEY_REGION_ID));
        this.world = Objects.requireNonNull(section.getString(KEY_WORLD_NAME));
        this.schematicProfile = section.getString(KEY_SCHEMATIC_PROFILE);

    }

    @Override
    public void load(@NotNull ConfigurationSection section) {
        lockWrite();
        try {
            load0(section);
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void removeLandlord() {
        lockWrite();
        try {
            this.landlord = null;
            this.lastKnownLandlordName = null;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void landlord(@NotNull UUID landlord, @NotNull String lastKnownName) {
        lockWrite();
        try {
            this.landlord = Objects.requireNonNull(landlord);
            this.lastKnownLandlordName = Objects.requireNonNull(lastKnownName);
        } finally {
            unlockWrite();
        }
    }


    @Override
    public @NotNull Optional<@NotNull UUID> landlord() {
        return Optional.ofNullable(this.landlord);
    }

    @Override
    public @NotNull Optional<@NotNull String> landlordLastKnownName() {
        lockWrite();
        try {
            return Optional.ofNullable(this.lastKnownLandlordName);
        } finally {
            unlockWrite();
        }
    }

    @Override
    public @NotNull String regionId() {
        lockRead();
        try {
            return this.regionId;
        } finally {
            unlockRead();
        }
    }

    @Override
    public @NotNull String worldName() {
        lockRead();
        try {
            return this.world;
        } finally {
            unlockRead();
        }
    }

    @Override
    public @NotNull Optional<@NotNull String> schematicProfile() {
        lockRead();
        try {
            return Optional.ofNullable(this.schematicProfile);
        } finally {
            unlockRead();
        }
    }

    @Override
    public @NotNull RegionStatus status() {
        lockRead();
        try {
            return this.regionStatus;
        } finally {
            unlockRead();
        }
    }

    @Override
    public boolean restoreEnabled() {
        lockRead();
        try {
            return this.restoreEnabled;
        } finally {
            unlockRead();
        }
    }

    @Override
    public boolean restrictedToRegion() {
        lockRead();
        try {
            return this.restrictedToRegion;
        } finally {
            unlockRead();
        }
    }

    @Override
    public boolean restrictedToWorld() {
        lockRead();
        try {
            return this.restrictedToWorld || restrictedToRegion();
        } finally {
            unlockRead();
        }
    }

    @Override
    public void regionId(@NotNull String regionId) {
        lockWrite();
        try {
            this.regionId = Objects.requireNonNull(regionId);
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void world(@NotNull World world) {
        lockWrite();
        try {
            this.world = world.getName();
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void world(@NotNull String world) {
        lockWrite();
        try {
            this.world = Objects.requireNonNull(world);
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void schematicProfile(@Nullable String schematicProfile) {
        lockWrite();
        try {
            this.schematicProfile = schematicProfile;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void status(@NotNull RegionStatus regionStatus) {
        lockWrite();
        try {
            this.regionStatus = regionStatus;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void restoreEnabled(boolean restoreEnabled) {
        lockWrite();
        try {
            this.restoreEnabled = restoreEnabled;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void restrictedToRegion(boolean restrictedToRegion) {
        lockWrite();
        try {
            this.restrictedToRegion = restrictedToRegion;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void restrictedToWorld(boolean restrictedToWorld) {
        lockWrite();
        try {
            this.restrictedToWorld = restrictedToWorld;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public boolean countForLimits() {
        lockRead();
        try {
            return this.countForLimits;
        } finally {
            unlockRead();
        }
    }

    @Override
    public void countForLimits(boolean countForLimits) {
        lockWrite();
        try {
            this.countForLimits = countForLimits;
        } finally {
            unlockWrite();
        }
    }

    @Override
    public void saveTo(@NotNull ConfigurationSection section) {
        section.set(KEY_REGION_ID, this.regionId);
        section.set(KEY_RESTORE, this.restoreEnabled);
        section.set(KEY_SCHEMATIC_PROFILE, this.schematicProfile);
        section.set(KEY_WORLD_NAME, this.world);
        section.set(KEY_LANDLORD, this.landlord);
        section.set(KEY_LANDLORD_NAME, this.lastKnownLandlordName);
        section.set(KEY_COUNT_FOR_LIMITS, this.countForLimits);
    }

    protected final void lockRead() {
        this.readWriteLock.readLock().lock();
    }

    protected final void unlockRead() {
        this.readWriteLock.readLock().unlock();
    }

    protected final void lockWrite() {
        this.readWriteLock.writeLock().lock();
    }

    protected final void unlockWrite() {
        this.readWriteLock.writeLock().unlock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractRegionMeta that = (AbstractRegionMeta) o;

        if (restoreEnabled != that.restoreEnabled) return false;
        if (restrictedToRegion != that.restrictedToRegion) return false;
        if (restrictedToWorld != that.restrictedToWorld) return false;
        if (countForLimits != that.countForLimits) return false;
        if (!regionId.equals(that.regionId)) return false;
        if (!world.equals(that.world)) return false;
        if (!Objects.equals(schematicProfile, that.schematicProfile))
            return false;
        if (regionStatus != that.regionStatus) return false;
        if (!Objects.equals(landlord, that.landlord)) return false;
        return Objects.equals(lastKnownLandlordName, that.lastKnownLandlordName);
    }

    @Override
    public int hashCode() {
        int result = regionId.hashCode();
        result = 31 * result + world.hashCode();
        result = 31 * result + (schematicProfile != null ? schematicProfile.hashCode() : 0);
        result = 31 * result + regionStatus.hashCode();
        result = 31 * result + (landlord != null ? landlord.hashCode() : 0);
        result = 31 * result + (lastKnownLandlordName != null ? lastKnownLandlordName.hashCode() : 0);
        result = 31 * result + (restoreEnabled ? 1 : 0);
        result = 31 * result + (restrictedToRegion ? 1 : 0);
        result = 31 * result + (restrictedToWorld ? 1 : 0);
        result = 31 * result + (countForLimits ? 1 : 0);
        return result;
    }
}
