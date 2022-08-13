package com.isatoltar.order.converter;

import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.enums.OrderStatus;
import com.isatoltar.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoConverter {

    public OrderResponse convert(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getFlavor(),
                order.getCrust(),
                order.getSize(),
                order.getOrderType(),
                OrderStatus.getNameFromValue(order.getOrderStatus())
        );
    }
}