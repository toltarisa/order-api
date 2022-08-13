package com.isatoltar.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum Flavor {

    HAWAII("HAWAII"),
    REGINA("REGINA"),
    QUATTRO_FORMAGGI("QUATTRO-FORMAGGI");

    String value;

    Flavor(String value) {
        this.value = value;
    }

    public static boolean isValid(String type) {
        boolean valid = false;
        for (Flavor flavor : Flavor.values()) {
            if (flavor.getValue().equals(type)) {
                valid = true;
                break;
            }
        }

        return valid;
    }
}
