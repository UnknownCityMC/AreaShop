package me.wiefferink.areashop.regions.util;

// Enum for schematic event types
    public enum RegionEvent {
        CREATED("created"),
        DELETED("deleted"),
        RENTED("rented"),
        EXTENDED("extended"),
        UNRENTED("unrented"),
        BOUGHT("bought"),
        SOLD("sold"),
        RESELL("resell");

        private final String value;

        RegionEvent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
