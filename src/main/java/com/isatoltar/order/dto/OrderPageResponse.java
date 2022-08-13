package com.isatoltar.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPageResponse {
    List<OrderResponse> orders;
    Integer currentPage;
    Long totalItems;
    Integer totalPages;
}

