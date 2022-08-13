package com.isatoltar.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum OrderStatus {
    CREATED(1, "CREATED"),
    CANCELLED(2, "CANCELLED"),
    DELIVERED(3, "DELIVERED");

    Integer value;
    String name;

    OrderStatus(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getNameFromValue(Integer value) {
        return Arrays.stream(OrderStatus.values())
                .filter(v -> v.getValue().equals(value))
                .map(OrderStatus::getName)
                .findFirst()
                .orElse(null);
    }
}
