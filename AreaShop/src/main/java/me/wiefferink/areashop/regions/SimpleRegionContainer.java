package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SimpleRegionContainer implements IRegionContainer {

    private final Map<String, GeneralRegionInterface> regions = new HashMap<>();
    private final Map<String, GeneralRegion> general = new HashMap<>();
    private final Map<String, BuyRegion> buys = new HashMap<>();
    private final Map<String, RentRegion> rents = new HashMap<>();

    private static @NotNull String sanitize(@NotNull String s) {
        return s.toLowerCase(Locale.ENGLISH);
    }

    private static @NotNull List<String> getTrueNames(@NotNull Collection<? extends GeneralRegionInterface> regions) {
        List<String> trueNames = new ArrayList<>(regions.size());
        for (GeneralRegionInterface region : regions) {
            trueNames.add(region.getName());
        }
        return trueNames;
    }

    @Nullable
    @Override
    public GeneralRegionInterface getRegionInterface(String name) {
        return this.regions.get(sanitize(name));
    }

    @Nullable
    @Override
    public GeneralRegion getRegion(String name) {
        return this.general.get(sanitize(name));
    }

    @Nullable
    @Override
    public RentRegion getRent(String name) {
        return this.rents.get(sanitize(name));
    }

    @Nullable
    @Override
    public BuyRegion getBuy(String name) {
        return this.buys.get(sanitize(name));
    }

    @Override
    public List<RentRegion> getRents() {
        return new ArrayList<>(this.rents.values());
    }

    @Override
    public Collection<RentRegion> getRentsRef() {
        return Collections.unmodifiableCollection(this.rents.values());
    }

    @Override
    public List<BuyRegion> getBuys() {
        return new ArrayList<>(this.buys.values());
    }

    @Override
    public Collection<BuyRegion> getBuysRef() {
        return Collections.unmodifiableCollection(this.buys.values());
    }

    @Override
    public List<GeneralRegion> getRegions() {
        return new ArrayList<>(this.general.values());
    }

    @Override
    public Collection<GeneralRegion> getRegionsRef() {
        return Collections.unmodifiableCollection(this.general.values());
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
    public List<String> getBuyNames() {
        return getTrueNames(this.buys.values());
    }

    @Override
    public List<String> getRentNames() {
        return getTrueNames(this.rents.values());
    }

    @Override
    public List<String> getRegionNames() {
        return getTrueNames(this.rents.values());
    }

    @Override
    public List<String> getRegionInterfaceNames() {
        return getTrueNames(this.regions.values());
    }

    @Override
    public void addRegionInterface(@NotNull GeneralRegionInterface region) {
        final String name = sanitize(region.getName());
        this.regions.put(name, region);
        if (region instanceof GeneralRegion generalRegion) {
            this.general.put(name, generalRegion);
            if (region instanceof BuyRegion buyRegion) {
                this.buys.put(name, buyRegion);
            } else if (region instanceof RentRegion rentRegion) {
                this.rents.put(name, rentRegion);
            }
        }
    }

    @Override
    public void removeRegion(@NotNull String region) {
        final String sanitized = sanitize(region);
        this.regions.remove(sanitized);
        this.general.remove(sanitized);
        this.buys.remove(sanitized);
        this.rents.remove(sanitized);
    }

    @Override
    public void removeRegion(@NotNull GeneralRegionInterface region) {
        removeRegion(region.getName());
    }

    @Override
    public void clearRegions() {
        this.regions.clear();
        this.general.clear();
        this.rents.clear();
        this.buys.clear();
    }
}
