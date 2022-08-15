package com.isatoltar.order.controller;

import com.isatoltar.order.dto.OrderPageResponse;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@Api(tags = "Orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    final OrderService orderService;

    @ApiOperation(value = "Create Order", notes = "Create single or multiple Orders")
    @PostMapping
    public ResponseEntity<List<OrderResponse>> createOrder(Principal principal,
                                                           @RequestBody @NotEmpty List<@Valid OrderRequest> orders) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getName(), orders));
    }

    @ApiOperation(value = "List All Orders", notes = "List All Orders with Pagination")
    @GetMapping
    public ResponseEntity<OrderPageResponse> listAllOrders(@RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer page,
                                                           @RequestParam(required = false, defaultValue = "10") @Positive Integer size,
                                                           @RequestParam(required = false, defaultValue = "id") @Size(max = 20) String sortBy) {

        Pageable paging = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.listAllOrders(paging));
    }

    @ApiOperation(value = "List All Orders of User")
    @GetMapping("/user")
    public ResponseEntity<List<OrderResponse>> listOrdersOfUser(@RequestParam @Positive Integer userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderService.listOrdersOfUser(userId));
    }

    @ApiOperation(value = "Cancel Order")
    @PatchMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable @Positive Integer orderId) {

        orderService.cancelOrder(orderId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @ApiOperation(value = "Delete Order")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable @Positive Integer orderId) {

        orderService.deleteOrder(orderId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}