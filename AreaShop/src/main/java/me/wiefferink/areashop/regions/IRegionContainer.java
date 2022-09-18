package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface IRegionContainer {

    @Nullable
    GeneralRegionInterface getRegionInterface(String name);

    /**
     * Get a region.
     *
     * @param name The name of the region to get (will be normalized)
     * @return The region if found, otherwise null
     */
    @Nullable
    GeneralRegion getRegion(String name);

    /**
     * Get a rental region.
     *
     * @param name The name of the rental region (will be normalized)
     * @return RentRegion if it could be found, otherwise null
     */
    @Nullable
    RentRegion getRent(String name);

    /**
     * Get a buy region.
     *
     * @param name The name of the buy region (will be normalized)
     * @return BuyRegion if it could be found, otherwise null
     */
    @Nullable
    BuyRegion getBuy(String name);

    /**
     * Get a copy of all registered {@link RentRegion} instances
     *
     * @return List of regions (it is safe to modify the list)
     */
    List<RentRegion> getRents();

    /**
     * Get a readonly view of all registered {@link RentRegion} instances.
     *
     * @return Readonly collection of all regions
     */
    Collection<RentRegion> getRentsRef();

    /**
     * Get a copy of all registered {@link BuyRegion} instances
     *
     * @return List of regions (it is safe to modify the list)
     */
    List<BuyRegion> getBuys();

    /**
     * Get a readonly view of all registered {@link BuyRegion} instances.
     *
     * @return Readonly collection of all regions
     */
    Collection<BuyRegion> getBuysRef();

    /**
     * Get copy of all registered {@link GeneralRegion}s.
     *
     * @return List of regions (it is safe to modify the list)
     */
    List<GeneralRegion> getRegions();

    /**
     * Get a readonly view of all registered {@link GeneralRegion} instances.
     *
     * @return Readonly collection of all regions
     */
    Collection<GeneralRegion> getRegionsRef();

    /**
     * Get copy of all registered {@link GeneralRegionInterface}s.
     *
     * @return List of regions (it is safe to modify the list)
     */
    List<GeneralRegionInterface> getRegionInterfaces();

    /**
     * Get a readonly view of all registered {@link GeneralRegionInterface} instances.
     *
     * @return Readonly collection of all regions
     */
    Collection<GeneralRegionInterface> getRegionInterfacesRef();

    /**
     * Get a list of names of all {@link BuyRegion}s
     *
     * @return A String list with all the names
     */
    List<String> getBuyNames();

    /**
     * Get a list of names of all {@link RentRegion}s
     *
     * @return A String list with all the names
     */
    List<String> getRentNames();

    /**
     * Get a list of names of all {@link GeneralRegion}
     *
     * @return A String list with all the names
     */
    List<String> getRegionNames();

    /**
     * Get a list of names of all {@link GeneralRegionInterface}s
     * @return A string list with all the names
     */
    List<String> getRegionInterfaceNames();

    /**
     * Register a {@link GeneralRegionInterface}
     * @param region The region to register
     */
    void addRegionInterface(@NotNull GeneralRegionInterface region);

    /**
     * Deregister a region by its name
     * @param region The name of the region to deregister
     * @see GeneralRegionInterface#getName()
     */
    void removeRegion(@NotNull String region);

    /**
     * Deregister a {@link GeneralRegionInterface}
     * @param region The region to deregister
     */
    void removeRegion(@NotNull GeneralRegionInterface region);

    /**
     * Clear all registered regions
     */
    void clearRegions();

}
