package com.isatoltar.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum Size {
    MEDIUM("M"),
    LARGE("L");

    String value;

    Size(String value) {
        this.value = value;
    }

    public static boolean isValid(String type) {
        boolean valid = false;
        for(Size size : Size.values()) {
            if(size.getValue().equals(type)) {
                valid = true;
                break;
            }
        }

        return valid;
    }
}
