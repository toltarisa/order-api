package com.isatoltar.order.controller;

import com.isatoltar.order.dto.OrderPageResponse;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;

    @PostMapping
    public ResponseEntity<List<OrderResponse>> createOrder(Principal principal,
                                                           @RequestBody List<OrderRequest> orders) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getName(), orders));
    }

    @GetMapping
    public ResponseEntity<OrderPageResponse> listAllOrders(@RequestParam(required = false, defaultValue = "0") @Positive Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") @Positive String sortBy) {

        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.listAllOrders(paging));
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> listOrdersOfUser(@RequestParam @Positive Integer userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.listOrdersOfUser(userId));
    }
}
