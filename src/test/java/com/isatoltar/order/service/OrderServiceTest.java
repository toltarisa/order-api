package com.isatoltar.order.service;

import com.isatoltar.order.converter.OrderDtoConverter;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.model.Order;
import com.isatoltar.order.model.User;
import com.isatoltar.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDtoConverter orderDtoConverter;

    @Mock
    private UserService userService;

    private OrderService orderService;

    private static ZonedDateTime NOW = ZonedDateTime.of(
            2022,
            8,
            16,
            23,
            7,
            35,
            0,
            ZoneId.systemDefault()
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clock.instant()).thenReturn(NOW.toInstant());
        orderService = new OrderService(
                clock,
                orderRepository,
                orderDtoConverter,
                userService
        );
    }

    @Test
    void itShouldCreateOrderWithValidRequest() {

        // given
        OrderRequest orderRequest = new OrderRequest(1, "REGINA", "THIN", "M");
        OrderResponse orderResponse = new OrderResponse(1, "REGINA", "THIN", "M", 1, "DINE-IN", "CREATED");
        User user = new User(1, "Isa Toltar", "isatoltar", "isatoltar", Set.of(), Set.of());
        Order order = Order.builder()
                .flavor(orderRequest.getFlavor())
                .crust(orderRequest.getCrust())
                .size(orderRequest.getSize())
                .tableNo(orderRequest.getTableNo())
                .timestamp(Timestamp.from(NOW.toInstant()))
                .user(user)
                .build();

        Order createdOrder = Order.builder()
                .id(1)
                .flavor(orderRequest.getFlavor())
                .crust(orderRequest.getCrust())
                .size(orderRequest.getSize())
                .tableNo(orderRequest.getTableNo())
                .timestamp(Timestamp.from(NOW.toInstant()))
                .user(user)
                .build();

        // when
        when(orderRepository.findAllByTableNoIn(Set.of(orderRequest.getTableNo()))).thenReturn(Optional.of(List.of()));
        when(userService.getUserByUsername("isatoltar")).thenReturn(Optional.of(user));
        when(orderRepository.save(order)).thenReturn(createdOrder);
        when(orderDtoConverter.convert(createdOrder)).thenReturn(orderResponse);

        List<OrderResponse> orderResponseList = orderService
                .createOrder("isatoltar", List.of(orderRequest));

        // then
        assertThat(List.of(orderResponse)).isEqualTo(orderResponseList);
        Mockito.verify(userService).getUserByUsername("isatoltar");
        Mockito.verify(orderRepository).save(order);
        Mockito.verify(orderRepository).findAllByTableNoIn(Set.of(orderRequest.getTableNo()));
        Mockito.verify(orderDtoConverter).convert(createdOrder);
    }

    @Test
    void listAllOrders() {
    }

    @Test
    void listOrdersOfUser() {
    }

    @Test
    void cancelOrder() {
    }

    @Test
    void deleteOrder() {
    }
}