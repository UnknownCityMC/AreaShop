package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.managers.IRegionContainer;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MockRegionContainer implements IRegionContainer {

    private Map<String, GeneralRegionInterface> regions = new HashMap<>();

    @Nullable
    @Override
    public GeneralRegionInterface getRegionInterface(String name) {
        return this.regions.get(name);
    }

    @Override
    public List<GeneralRegionInterface> getRegionInterfaces() {
        return new ArrayList<>(this.regions.values());
    }

    @Override
    public Collection<GeneralRegionInterface> getRegionInterfacesRef() {
        return Collections.unmodifiableCollection(this.regions.values());
    }

    @Override
    public void addRegionInterface(GeneralRegionInterface region) {
        this.regions.put(region.getName(), region);
    }

    @Override
    public void removeRegion(String region) {
        this.regions.remove(region);
    }

    @Override
    public void removeRegion(GeneralRegionInterface region) {
        this.regions.remove(region.getName());
    }

    @Nullable
    @Override
    public GeneralRegion getRegion(String name) {
        final GeneralRegionInterface region = this.regions.get(name);
        return region instanceof GeneralRegion generalRegion ? generalRegion : null;
    }

    @Nullable
    @Override
    public RentRegion getRent(String name) {
        final GeneralRegionInterface region = this.regions.get(name);
        return region instanceof RentRegion rentRegion ? rentRegion : null;
    }

    @Nullable
    @Override
    public BuyRegion getBuy(String name) {
        final GeneralRegionInterface region = this.regions.get(name);
        return region instanceof BuyRegion buyRegion ? buyRegion : null;
    }

    @Override
    public Collection<RentRegion> getRents() {
        return this.regions.values().stream()
                .filter(region -> region instanceof RentRegion)
                .map(RentRegion.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<RentRegion> getRentsRef() {
        return getRents();
    }

    @Override
    public List<BuyRegion> getBuys() {
        return this.regions.values().stream()
                .filter(region -> region instanceof BuyRegion)
                .map(BuyRegion.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BuyRegion> getBuysRef() {
        return getBuys();
    }

    @Override
    public List<GeneralRegion> getRegions() {
        return this.regions.values().stream().filter(region -> region instanceof GeneralRegion)
                .map(GeneralRegion.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<GeneralRegion> getRegionsRef() {
        return getRegions();
    }

    @Override
    public List<String> getBuyNames() {
        return getBuys().stream().map(GeneralRegionInterface::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getRentNames() {
        return getRents().stream().map(GeneralRegionInterface::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getRegionNames() {
        return this.regions.values().stream().map(GeneralRegionInterface::getName).collect(Collectors.toList());
    }
}
