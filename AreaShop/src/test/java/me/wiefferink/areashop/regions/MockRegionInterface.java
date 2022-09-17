package me.wiefferink.areashop.regions;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import org.bukkit.World;

import java.text.DateFormat;
import java.util.Calendar;

public record MockRegionInterface(ProtectedRegion region, String name, World world, Calendar calendar, DateFormat dateFormat) implements GeneralRegionInterface, ReplacementProvider {

    
    
    @Override
    public ProtectedRegion getRegion() {
        return this.region;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public String getWorldName() {
        return this.world.getName();
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public Object provideReplacement(String variable) {
        return switch (variable) {
            case AreaShop.tagRegionName -> this.region.getId();
            case AreaShop.tagWorldName -> this.world.getName();
            case AreaShop.tagWidth -> getWidth();
            case AreaShop.tagDepth ->  getDepth();
            case AreaShop.tagHeight -> getHeight();
            // Date/time
            case AreaShop.tagEpoch -> this.calendar.getTimeInMillis();
            case AreaShop.tagMillisecond -> this.calendar.get(Calendar.MILLISECOND);
            case AreaShop.tagSecond -> this.calendar.get(Calendar.SECOND);
            case AreaShop.tagMinute -> this.calendar.get(Calendar.MINUTE);
            case AreaShop.tagHour -> this.calendar.get(Calendar.HOUR_OF_DAY);
            case AreaShop.tagDay -> this.calendar.get(Calendar.DAY_OF_MONTH);
            case AreaShop.tagMonth -> this.calendar.get(Calendar.MONTH) + 1;
            case AreaShop.tagYear -> this.calendar.get(Calendar.YEAR);
            case AreaShop.tagDateTime, AreaShop.tagDateTimeShort -> this.dateFormat.format(this.calendar.getTime());
            default -> null;
        };
    }
}
