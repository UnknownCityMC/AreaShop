package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;

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
     * Get all rental regions.
     *
     * @return List of all rental regions
     */
    Collection<RentRegion> getRents();

    /**
     * Get all rental regions.
     *
     * @return List of all rental regions
     */
    Collection<RentRegion> getRentsRef();

    /**
     * Get all buy regions.
     *
     * @return List of all buy regions
     */
    List<BuyRegion> getBuys();

    Collection<BuyRegion> getBuysRef();

    /**
     * Get all regions.
     *
     * @return List of all regions (it is safe to modify the list)
     */
    List<GeneralRegion> getRegions();

    /**
     * Get all regions.
     *
     * @return List of all regions (it is safe to modify the list)
     */
    Collection<GeneralRegion> getRegionsRef();

    List<GeneralRegionInterface> getRegionInterfaces();

    Collection<GeneralRegionInterface> getRegionInterfacesRef();

    /**
     * Get a list of names of all buy regions.
     *
     * @return A String list with all the names
     */
    List<String> getBuyNames();

    /**
     * Get a list of names of all rent regions.
     *
     * @return A String list with all the names
     */
    List<String> getRentNames();

    /**
     * Get a list of names of all regions.
     *
     * @return A String list with all the names
     */
    List<String> getRegionNames();

    void addRegionInterface(GeneralRegionInterface region);

    void removeRegion(String region);

    void removeRegion(GeneralRegionInterface region);

}
