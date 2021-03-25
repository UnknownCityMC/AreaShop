package me.wiefferink.areashop.regions.util;

// Enum for click types
    public enum ClickType {
        RIGHTCLICK("rightClick"),
        LEFTCLICK("leftClick"),
        SHIFTRIGHTCLICK("shiftRightClick"),
        SHIFTLEFTCLICK("shiftLeftClick");

        private final String value;

        ClickType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
