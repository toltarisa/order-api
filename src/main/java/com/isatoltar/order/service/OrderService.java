package com.isatoltar.order.service;

import com.isatoltar.order.converter.OrderDtoConverter;
import com.isatoltar.order.dto.OrderPageResponse;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.enums.Crust;
import com.isatoltar.order.enums.Flavor;
import com.isatoltar.order.enums.OrderStatus;
import com.isatoltar.order.enums.Size;
import com.isatoltar.order.exception.BadRequestException;
import com.isatoltar.order.exception.ResourceAlreadyExistsException;
import com.isatoltar.order.exception.ResourceNotFoundException;
import com.isatoltar.order.model.Order;
import com.isatoltar.order.model.User;
import com.isatoltar.order.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderService {

    private final Integer DELIVERY_ORDER_CONSTRAINT = 100_000;

    final Clock clock;
    final OrderRepository orderRepository;
    final OrderDtoConverter orderDtoConverter;
    final UserService userService;

    public List<OrderResponse> createOrder(String username, List<OrderRequest> orderRequests) {

        /*
         * Since we can get multiple orders in this endpoint, after validating orders,
         * we can save orders to database via bulk save or asynchronous processing via @Async or some kind of queue
         * For simplicity i will loop through each order and save order to database
         */

        validateTableStatus(orderRequests);

        User user = userService.getUserByUsername(username).orElse(null);
        List<Order> orders = new ArrayList<>();

        orderRequests.forEach(orderRequest -> {
            validateOrder(orderRequest);
            Order order = Order.builder()
                    .flavor(orderRequest.getFlavor())
                    .crust(orderRequest.getCrust())
                    .size(orderRequest.getSize())
                    .tableNo(orderRequest.getTableNo())
                    .timestamp(Timestamp.from(Instant.now(clock)))
                    .user(user)
                    .build();

            if (orderRequest.getTableNo() > DELIVERY_ORDER_CONSTRAINT)
                order.setOrderType("DELIVERY");

            orders.add(order);
        });

        return orders.stream()
                .map(order -> orderDtoConverter.convert(orderRepository.save(order)))
                .collect(Collectors.toList());
    }

    public OrderPageResponse listAllOrders(Pageable paging) {

        Page<Order> orderPage = orderRepository.findAll(paging);
        if (orderPage.isEmpty())
            return new OrderPageResponse(List.of(), 0, 0L, 0);

        List<OrderResponse> orders = orderPage.getContent()
                .stream()
                .map(orderDtoConverter::convert)
                .collect(Collectors.toList());

        return new OrderPageResponse(
                orders,
                orderPage.getNumber(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    public List<OrderResponse> listOrdersOfUser(Integer userId) {

        Boolean userExists = userService.doesUserExists(userId);
        if(!userExists)
            throw new ResourceNotFoundException("User with id = " + userId + " does not exists");

        return orderRepository.getAllByUserId(userId)
                .orElse(List.of())
                .stream()
                .map(orderDtoConverter::convert)
                .collect(Collectors.toList());
    }

    private void validateTableStatus(List<OrderRequest> orderRequests) {
        Set<Integer> tableNumbers = orderRequests.stream()
                .map(OrderRequest::getTableNo)
                .collect(Collectors.toSet());

        List<Order> ordersByTableNumbers = orderRepository.findAllByTableNoIn(tableNumbers).orElse(List.of());
        if (!ordersByTableNumbers.isEmpty()) {
            throw new ResourceAlreadyExistsException(
                    String.format(
                            "Table numbers %s are already ordered",
                            ordersByTableNumbers.stream()
                                    .map(order -> String.valueOf(order.getTableNo()))
                                    .collect(Collectors.joining(","))
                    )
            );
        }
    }

    private void validateOrder(OrderRequest orderRequest) {
        String crust = orderRequest.getCrust();
        String flavor = orderRequest.getFlavor();
        String size = orderRequest.getSize();

        if (!Crust.isValid(crust))
            throw new BadRequestException("Crust = " + crust + " is not valid!");

        if (!Flavor.isValid(flavor))
            throw new BadRequestException("Flavor = " + flavor + " is not valid!");

        if (!Size.isValid(size))
            throw new BadRequestException("Size = " + size + " is not valid!");
    }

    public void cancelOrder(Integer orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = " + orderId + " does not exists"));

        order.setOrderStatus(OrderStatus.CANCELLED.getValue());

        orderRepository.save(order);
    }

    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = " + orderId + " does not exists"));

        orderRepository.delete(order);
    }
}