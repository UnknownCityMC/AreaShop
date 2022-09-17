package me.wiefferink.areashop.hook.placeholders;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.managers.IRegionContainer;
import me.wiefferink.areashop.managers.MockRegionContainer;
import me.wiefferink.areashop.regions.MockRegionInterface;
import org.bukkit.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class TestPlaceholders {

    private static ServerMock server;

    @BeforeAll
    static void init() {
        server = MockBukkit.mock();
    }

    @AfterAll
    static void teardown() {
        server = null;
        MockBukkit.unmock();
    }

    @Test
    void testPlaceholder() {
        // Setup mock regions
        IRegionContainer regionContainer = new MockRegionContainer();
        final World world = server.addSimpleWorld("test");
        final String regionId = "test-region";
        final BlockVector3 location = BlockVector3.ZERO;
        ProtectedRegion protectedRegion = new ProtectedCuboidRegion(regionId, location, location);
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = new SimpleDateFormat();
        final NumberFormat numberFormat = NumberFormat.getInstance();
        final GeneralRegionInterface region = new MockRegionInterface(protectedRegion, regionId, world, calendar, dateFormat);
        regionContainer.addRegionInterface(region);
        // Test the expansion
        PlaceholderExpansion expansion = new BuiltinRegionExpansion(regionContainer);
        final String actualRegionName = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagRegionName));
        Assertions.assertEquals(regionId, actualRegionName);
        final String actualWorldName = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagWorldName));
        Assertions.assertEquals(world.getName(), actualWorldName);
        final String zero = "0";
        for (String tag : new String[]{AreaShop.tagWidth, AreaShop.tagHeight, AreaShop.tagWidth}) {
            final String actual = expansion.onRequest(null, String.format("%s_%s", regionId, tag));
            Assertions.assertEquals(zero, actual);
        }
        final String actualEpoch = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagEpoch));
        Assertions.assertEquals(numberFormat.format(calendar.getTimeInMillis()), actualEpoch);
        final String actualMillis = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagMillisecond));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.MILLISECOND)), actualMillis);
        final String actualSecond = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagSecond));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.SECOND)), actualSecond);
        final String actualMinute = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagMinute));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.MINUTE)), actualMinute);
        final String actualHour = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagHour));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.HOUR_OF_DAY)), actualHour);
        final String actualDay = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagDay));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.DAY_OF_MONTH)), actualDay);
        final String actualMonth = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagMonth));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.MONTH) + 1), actualMonth);
        final String actualYear = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagYear));
        Assertions.assertEquals(numberFormat.format(calendar.get(Calendar.YEAR)), actualYear);
        final String actualDate = expansion.onRequest(null, String.format("%s_%s", regionId, AreaShop.tagDateTime));
        Assertions.assertEquals(dateFormat.format(calendar.getTime()), actualDate);
    }


}
