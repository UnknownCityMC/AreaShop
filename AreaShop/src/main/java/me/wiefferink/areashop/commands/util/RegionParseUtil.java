package me.wiefferink.areashop.commands.util;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.spongepowered.configurate.NodePath;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public final class RegionParseUtil {

    private RegionParseUtil() {
        throw new IllegalStateException("Cannot instantiate static utility class");
    }

    @Nonnull
    public static Collection<GeneralRegion> getOrParseRegionsInSel(
            @Nonnull CommandContext<?> context,
            @Nonnull CommandSender sender
    ) {
        if (!(sender instanceof Player player)) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-we", "only-player"));
        }

        GeneralRegion declaredRegion = context.getOrDefault("region", null);
        if (declaredRegion != null) {
            return List.of(declaredRegion);
        }

        Location location = player.getLocation();
        List<GeneralRegion> regions = Utils.getImportantRegions(location);
        if (!regions.isEmpty()) {
            return regions;
        }

        throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "no-found"));
    }


    @Nonnull
    public static GeneralRegion getOrParseRegion(
            @Nonnull CommandContext<?> context,
            @Nonnull CommandSender sender
    ) throws AreaShopCommandException {
        GeneralRegion region = context.getOrDefault("region", null);
        if (region != null) {
            return region;
        }

        if (!(sender instanceof Entity entity)) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "only-player"));
        }
        Location location = entity.getLocation();

        List<GeneralRegion> regions = Utils.getImportantRegions(location);
        throwLocationError(regions);
        return regions.getFirst();
    }

    @Nonnull
    public static BuyRegion getOrParseBuyRegion(
            @Nonnull CommandContext<?> context,
            @Nonnull CommandSender sender
    ) {
        BuyRegion buyRegion = context.getOrDefault("region-buy", null);
        if (buyRegion != null) {
            return buyRegion;
        }
        if (!(sender instanceof Player player)) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "only-player"));
        }
        List<BuyRegion> regions = Utils.getImportantBuyRegions(player.getLocation());
        throwLocationError(regions);
        return regions.getFirst();
    }

    @Nonnull
    public static RentRegion getOrParseRentRegion(
            @Nonnull CommandContext<?> context,
            @Nonnull CommandSender sender
    ) {
        RentRegion rentRegion = context.getOrDefault("region-rent", null);
        if (rentRegion != null) {
            return rentRegion;
        }
        if (!(sender instanceof Player player)) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "only-player"));
        }

        List<RentRegion> regions = Utils.getImportantRentRegions(player.getLocation());
        throwLocationError(regions);
        return regions.getFirst();
    }

    private static void throwLocationError(List<? extends GeneralRegion> regions) {
        if (regions.isEmpty()) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "no-found"));
        } else if (regions.size() != 1) {
            throw new AreaShopCommandException(NodePath.path("exception", "region-at-location", "multiple-found"));
        }
    }
}
