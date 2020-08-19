package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import org.bstats.bukkit.Metrics;

import java.util.HashMap;
import java.util.Map;

public class Analytics {

    private Analytics() {

    }

    /**
     * Start analytics tracking.
     */
    public static void start() {
        // bStats statistics
        try {
            Metrics metrics = new Metrics(AreaShop.getInstance());

            // Number of regions
            metrics.addCustomChart(new Metrics.SingleLineChart("region_count", () -> AreaShop.getInstance().getFileManager().getRegions().size()));

            // Number of rental regions
            metrics.addCustomChart(new Metrics.SingleLineChart("rental_region_count", () -> {
                return AreaShop.getInstance().getFileManager().getRents().size();
            }));

            // Number of buy regions
            metrics.addCustomChart(new Metrics.SingleLineChart("buy_region_count", () -> AreaShop.getInstance().getFileManager().getBuys().size()));

            // Language
            metrics.addCustomChart(new Metrics.SimplePie("language", () -> AreaShop.getInstance().getConfig().getString("language")));

            // Pie with region states
            metrics.addCustomChart(new Metrics.AdvancedPie("region_state", () -> {
                Map<String, Integer> map = new HashMap<>();
                RegionStateStats stats = getStateStats();
                map.put("For Rent", stats.forrent);
                map.put("Rented", stats.rented);
                map.put("For Sale", stats.forsale);
                map.put("Sold", stats.sold);
                map.put("Reselling", stats.reselling);
                return map;
            }));

            // Time series of each region state
            metrics.addCustomChart(new Metrics.SingleLineChart("forrent_region_count", () -> getStateStats().forrent));

            metrics.addCustomChart(new Metrics.SingleLineChart("rented_region_count", () -> getStateStats().rented));

            metrics.addCustomChart(new Metrics.SingleLineChart("forsale_region_count", () -> getStateStats().forsale));

            metrics.addCustomChart(new Metrics.SingleLineChart("sold_region_count", () -> getStateStats().sold));

            metrics.addCustomChart(new Metrics.SingleLineChart("reselling_region_count", () -> getStateStats().reselling));

            // TODO track rent/buy/unrent/sell/resell actions (so that it can be reported per collection interval)

            AreaShop.debug("Started bstats.org statistics service");
        } catch (Exception e) {
            AreaShop.debug("Could not start bstats.org statistics service");
        }
    }

    private static RegionStateStats getStateStats() {
        RegionStateStats result = new RegionStateStats();
        for (GeneralRegion region : AreaShop.getInstance().getFileManager().getRegions()) {
            if (region instanceof RentRegion) {
                RentRegion rent = (RentRegion) region;
                if (rent.isAvailable()) {
                    result.forrent++;
                } else {
                    result.rented++;
                }
            } else if (region instanceof BuyRegion) {
                BuyRegion buy = (BuyRegion) region;
                if (buy.isAvailable()) {
                    result.forsale++;
                } else if (buy.isInResellingMode()) {
                    result.reselling++;
                } else {
                    result.sold++;
                }
            }
        }
        return result;
    }

    private static class RegionStateStats {
        int forrent = 0;
        int forsale = 0;
        int rented = 0;
        int sold = 0;
        int reselling = 0;
    }

}
