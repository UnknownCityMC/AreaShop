package me.wiefferink.areashop.regions.util;

// Enum for limit types
    public enum LimitType {
        RENTS("rents"),
        BUYS("buys"),
        TOTAL("total"),
        EXTEND("extend");

        private final String value;

        LimitType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
