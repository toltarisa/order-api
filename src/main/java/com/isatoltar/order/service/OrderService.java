package com.isatoltar.order.service;

import com.isatoltar.order.converter.OrderDtoConverter;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.enums.Crust;
import com.isatoltar.order.enums.Flavor;
import com.isatoltar.order.enums.Size;
import com.isatoltar.order.exception.BadRequestException;
import com.isatoltar.order.exception.ResourceAlreadyExistsException;
import com.isatoltar.order.model.Order;
import com.isatoltar.order.model.User;
import com.isatoltar.order.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderService {

    final OrderRepository orderRepository;
    final OrderDtoConverter orderDtoConverter;
    final UserService userService;

    public List<OrderResponse> createOrder(String username, List<OrderRequest> orderRequests) {

        /*
         * Since we can get multiple orders in this endpoint, after validating orders, we can create a queue (rabbit mq, kafka etc.)
         * and put each order in queue and process it asynchronously for better performance.
         * For simplicity i will loop through each order and send request to pizzeria API
         */

        validateTableStatus(orderRequests);

        User user = userService.getUserByUsername(username).orElse(null);
        List<Order> orders = new ArrayList<>();

        orderRequests.forEach(orderRequest -> {
            validateOrder(orderRequest);
            orders.add(
                    Order.builder()
                            .flavor(orderRequest.getFlavor())
                            .crust(orderRequest.getCrust())
                            .size(orderRequest.getSize())
                            .tableNo(orderRequest.getTableNo())
                            .user(user)
                            .build()
            );
        });

        return orders.stream()
                .map(order -> orderDtoConverter.convert(orderRepository.save(order)))
                .collect(Collectors.toList());
    }

    private void validateTableStatus(List<OrderRequest> orderRequests) {
        Set<Integer> tableNumbers = orderRequests.stream()
                .map(OrderRequest::getTableNo)
                .collect(Collectors.toSet());

        List<Order> ordersByTableNumbers = getAllOrdersByTableNumbers(tableNumbers);
        if (!ordersByTableNumbers.isEmpty()) {
            throw new ResourceAlreadyExistsException(
                    String.format(
                            "Orders for table numbers %s already exists",
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

    private List<Order> getAllOrdersByTableNumbers(Set<Integer> tableNumbers) {
        return orderRepository.findAllByTableNoIn(tableNumbers)
                .orElse(List.of());
    }
}