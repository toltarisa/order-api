package com.isatoltar.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum Crust {
    THIN("THIN"),
    NORMAL("NORMAL");

    String value;

    Crust(String value) {
        this.value = value;
    }

    public static boolean isValid(String value) {
        boolean valid = false;
        for (Crust crust : Crust.values()) {
            if (crust.getValue().equals(value)) {
                valid = true;
                break;
            }
        }

        return valid;
    }
}
