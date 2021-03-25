package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.regions.util.RegionStatus;
import me.wiefferink.areashop.regions.util.RegionType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface RegionMeta {

    void load(@NotNull ConfigurationSection section);

    @NotNull String regionId();

    void regionId(@NotNull String regionId);

    @NotNull String worldName();

    @NotNull Optional<@NotNull UUID> landlord();

    @NotNull Optional<@NotNull String> landlordLastKnownName();

    void removeLandlord();

    void landlord(@NotNull UUID landlord, @NotNull String lastKnownName);

    default void landlord(@NotNull Player player) {
        landlord(player.getUniqueId(), player.getName());
    }

    void world(@NotNull World world);

    void world(@NotNull String worldName);

    @NotNull Optional<@NotNull String> schematicProfile();

    void schematicProfile(@Nullable String schematicProfile);

    @NotNull RegionStatus status();

    void status(@NotNull RegionStatus status);

    boolean restoreEnabled();

    void restoreEnabled(boolean enabled);

    boolean restrictedToRegion();

    void restrictedToRegion(boolean restricted);

    boolean restrictedToWorld();

    void restrictedToWorld(boolean restricted);

    boolean countForLimits();

    void countForLimits(boolean counts);

    void saveTo(@NotNull ConfigurationSection configurationSection);
}
