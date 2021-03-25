package me.wiefferink.areashop.regions.util;

// Enum for region types
    public enum RegionType {
        RENT("rent"),
        BUY("buy");

        private final String value;

        RegionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
