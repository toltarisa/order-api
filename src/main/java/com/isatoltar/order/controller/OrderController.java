package com.isatoltar.order.controller;

import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('list_all_orders')")
    public ResponseEntity<List<OrderResponse>> createOrder(Principal principal,
                                                           @RequestBody List<OrderRequest> orders) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getName(), orders));
    }
}
