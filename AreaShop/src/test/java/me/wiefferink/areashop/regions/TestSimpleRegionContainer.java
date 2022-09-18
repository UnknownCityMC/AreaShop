package me.wiefferink.areashop.regions;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

class TestSimpleRegionContainer {

    private static ServerMock server;

    @BeforeAll
    static void init() {
        server = MockBukkit.mock();
    }

    @AfterAll
    static void teardown() {
        MockBukkit.unmock();
    }

    static GeneralRegionInterface generateRegion(@NotNull String name) {
        final ProtectedRegion region = new ProtectedCuboidRegion("null", BlockVector3.ZERO, BlockVector3.ZERO);
        final World world = server.addSimpleWorld("world");
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = new SimpleDateFormat();
        return new MockRegionInterface(region, name, world, calendar, dateFormat);
    }

    @Test
    void testRegionAddRemove() {
        final IRegionContainer regionContainer = new SimpleRegionContainer();
        final String sanitizedName = "test";
        final String dirtyName = "TEST";
        final GeneralRegionInterface regionInterface = generateRegion(sanitizedName);
        Assertions.assertNull(regionContainer.getRegion(sanitizedName));
        Assertions.assertNull(regionContainer.getRegion(dirtyName));
        regionContainer.addRegionInterface(regionInterface);
        Assertions.assertSame(regionInterface, regionContainer.getRegionInterface(sanitizedName));
        Assertions.assertSame(regionInterface, regionContainer.getRegionInterface(dirtyName));
        final List<String> actualNames = regionContainer.getRegionInterfaceNames();
        Assertions.assertEquals(1, actualNames.size());
        Assertions.assertEquals(sanitizedName, actualNames.get(0));
    }

    @Test
    void testCollectionGetters() {
        final IRegionContainer regionContainer = new SimpleRegionContainer();
        final String sanitizedName = "test";
        final GeneralRegionInterface regionInterface = generateRegion(sanitizedName);
        regionContainer.addRegionInterface(regionInterface);
        testCollectionAndRef(1, regionContainer.getRegionInterfacesRef(), regionContainer.getRegionInterfaces());
        final Collection<GeneralRegionInterface> regionInterfaces = regionContainer.getRegionInterfaces();
        Assertions.assertTrue(regionInterfaces.contains(regionInterface));
        testCollectionAndRef(0, regionContainer.getRegionsRef(), regionContainer.getRegions());
        testCollectionAndRef(0, regionContainer.getBuysRef(), regionContainer.getBuys());
        testCollectionAndRef(0, regionContainer.getRentsRef(), regionContainer.getRents());

    }

    private static  <T> void testCollectionAndRef(int expectedSize, Collection<T> ref, Collection<T> copy) {
        Assertions.assertEquals(expectedSize, ref.size());
        Assertions.assertEquals(ref.size(), copy.size());
        try {
            copy.clear();
        } catch (Throwable ex) {
            Assertions.fail("Failed to modify the copy", ex);
            return;
        }
        Assertions.assertThrows(UnsupportedOperationException.class, ref::clear);
    }



}
