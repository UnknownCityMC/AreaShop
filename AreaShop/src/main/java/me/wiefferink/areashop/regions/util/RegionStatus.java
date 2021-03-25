package me.wiefferink.areashop.regions.util;

// Enum for Region states
public enum RegionStatus {

    FORRENT("forrent"),
    RENTED("rented"),
    FORSALE("forsale"),
    SOLD("sold"),
    RESELL("resell");

    private final String value;

    RegionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
