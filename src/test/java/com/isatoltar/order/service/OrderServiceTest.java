package com.isatoltar.order.service;

import com.isatoltar.order.converter.OrderDtoConverter;
import com.isatoltar.order.dto.OrderPageResponse;
import com.isatoltar.order.dto.OrderRequest;
import com.isatoltar.order.dto.OrderResponse;
import com.isatoltar.order.enums.OrderStatus;
import com.isatoltar.order.exception.BadRequestException;
import com.isatoltar.order.exception.ResourceAlreadyExistsException;
import com.isatoltar.order.exception.ResourceNotFoundException;
import com.isatoltar.order.model.Order;
import com.isatoltar.order.model.User;
import com.isatoltar.order.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderServiceTest {

    @Mock Clock clock;
    @Mock OrderRepository orderRepository;
    @Mock OrderDtoConverter orderDtoConverter;
    @Mock UserService userService;

    OrderService orderService;

    static ZonedDateTime NOW = ZonedDateTime.of(2022, 8, 16, 23, 7, 35, 0, ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(
                clock,
                orderRepository,
                orderDtoConverter,
                userService
        );
    }

    @Test
    @DisplayName("It should create order with valid request")
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
        when(clock.instant()).thenReturn(NOW.toInstant());
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
    @DisplayName("It should throw exception when table already have order")
    void itShouldThrowExceptionWhenTableAlreadyHaveOrder() {

        // given
        OrderRequest orderRequest = new OrderRequest(1, "REGINA", "THIN", "M");
        User user = new User(1, "Isa Toltar", "isatoltar", "isatoltar", Set.of(), Set.of());
        Order order = Order.builder()
                .flavor(orderRequest.getFlavor())
                .crust(orderRequest.getCrust())
                .size(orderRequest.getSize())
                .tableNo(orderRequest.getTableNo())
                .timestamp(Timestamp.from(NOW.toInstant()))
                .user(user)
                .build();

        // when
        when(orderRepository.findAllByTableNoIn(Set.of(orderRequest.getTableNo())))
                .thenReturn(Optional.of(List.of(order)));

        // then
        assertThatThrownBy(() -> orderService.createOrder("isatoltar", List.of(orderRequest)))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(String.format("Table numbers %s are already ordered", orderRequest.getTableNo()));
    }

    @Test
    @DisplayName("When flavor is invalid it should throw bad request exception")
    void whenFlavorIsInvalidItShouldThrowBadRequestException() {
        OrderRequest orderRequest = new OrderRequest(1, "Neapolitan", "THIN", "M");

        assertThatThrownBy(() -> orderService.createOrder("isatoltar", List.of(orderRequest)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(String.format("Flavor = %s is not valid!", orderRequest.getFlavor()));
    }

    @Test
    @DisplayName("When crust is invalid it should throw bad request exception")
    void whenCrustIsInvalidItShouldThrowBadRequestException() {
        OrderRequest orderRequest = new OrderRequest(1, "REGGINA", "STUFFED", "M");

        assertThatThrownBy(() -> orderService.createOrder("isatoltar", List.of(orderRequest)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(String.format("Crust = %s is not valid!", orderRequest.getCrust()));
    }

    @Test
    @DisplayName("When size is invalid it should throw bad request exception")
    void whenSizeInvalidItShouldThrowBadRequestException() {
        OrderRequest orderRequest = new OrderRequest(1, "REGINA", "THIN", "XL");

        assertThatThrownBy(() -> orderService.createOrder("isatoltar", List.of(orderRequest)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(String.format("Size = %s is not valid!", orderRequest.getSize()));
    }

    @Test
    @DisplayName("When tabel number is greater than 100.000 it should set deliveryType as \"DELIVERY\" ")
    void whenTableNumberIsGreaterThan100_000ItShouldSetDeliveryTypeAsDelivery() {

        //given
        OrderRequest orderRequest = new OrderRequest(100001, "REGINA", "THIN", "L");
        OrderResponse orderResponse = new OrderResponse(1, "REGINA", "THIN", "L", 100001, "DELIVERY", "CREATED");
        User user = new User(1, "Isa Toltar", "isatoltar", "isatoltar", Set.of(), Set.of());
        Order order = Order.builder()
                .flavor(orderRequest.getFlavor())
                .crust(orderRequest.getCrust())
                .size(orderRequest.getSize())
                .orderType("DELIVERY")
                .tableNo(orderRequest.getTableNo())
                .timestamp(Timestamp.from(NOW.toInstant()))
                .user(user)
                .build();

        Order createdOrder = Order.builder()
                .id(1)
                .flavor(orderRequest.getFlavor())
                .crust(orderRequest.getCrust())
                .size(orderRequest.getSize())
                .orderType("DELIVERY")
                .tableNo(orderRequest.getTableNo())
                .timestamp(Timestamp.from(NOW.toInstant()))
                .user(user)
                .build();

        // when
        when(clock.instant()).thenReturn(NOW.toInstant());
        when(orderRepository.findAllByTableNoIn(Set.of(orderRequest.getTableNo()))).thenReturn(Optional.of(List.of()));
        when(userService.getUserByUsername("isatoltar")).thenReturn(Optional.of(user));
        when(orderRepository.save(order)).thenReturn(createdOrder);
        when(orderDtoConverter.convert(createdOrder)).thenReturn(orderResponse);


        // then
        List<OrderResponse> orderResponseList = orderService
                .createOrder("isatoltar", List.of(orderRequest));

        assertThat(orderResponseList.get(0).getOrderType()).isEqualTo("DELIVERY");
    }

    @Test
    @DisplayName("List all endpoint shoud list all orders when request parameters are valid")
    void itShouldListAllOrdersWhenRequestParamsAreValid() {

        // given
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("id"));
        OrderResponse orderResponse1 = new OrderResponse(1, "REGINA", "THIN", "M", 1, "DINE-IN", "CREATED");
        OrderResponse orderResponse2 = new OrderResponse(2, "HAWAII", "NORMAL", "L", 2, "DINE-IN", "CREATED");
        Order order1 = Order.builder()
                .id(1)
                .flavor("REGINA")
                .crust("THIN")
                .size("M")
                .orderType("DINE-IN")
                .tableNo(1)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();
        Order order2 = Order.builder()
                .id(2)
                .flavor("HAWAII")
                .crust("NORMAL")
                .size("L")
                .orderType("DINE-IN")
                .tableNo(2)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();
        PageImpl<Order> orderPage = new PageImpl<>(List.of(order1, order2));

        // when
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderDtoConverter.convert(order1)).thenReturn(orderResponse1);
        when(orderDtoConverter.convert(order2)).thenReturn(orderResponse2);

        // then
        OrderPageResponse response = orderService.listAllOrders(pageable);

        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getOrders()).isEqualTo(List.of(orderResponse1, orderResponse2));

    }

    @Test
    @DisplayName("List all orders endpoint should return empty response object when there is not any order")
    void ItShouldReturnEmptyResponseObjectWhenThereIsNotAnyOrder() {

        // given
        PageRequest pageable = PageRequest.of(0, 5, Sort.by("id"));
        PageImpl<Order> orderPage = new PageImpl<>(List.of());

        // when
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // then
        OrderPageResponse response = orderService.listAllOrders(pageable);

        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalItems()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getOrders()).isEqualTo(List.of());
    }

    @Test
    @DisplayName("It should list orders of user when given user is exists")
    void itShouldListOrdersOfUserWhenGivenUserIsExists() {

        // given
        User user = new User(1, "Isa Toltar", "isatoltar", "isatoltar", Set.of(), Set.of());
        OrderResponse orderResponse1 = new OrderResponse(1, "REGINA", "THIN", "M", 1, "DINE-IN", "CREATED");
        OrderResponse orderResponse2 = new OrderResponse(2, "HAWAII", "NORMAL", "L", 2, "DINE-IN", "CREATED");
        Order order1 = Order.builder()
                .id(1)
                .flavor("REGINA")
                .crust("THIN")
                .size("M")
                .orderType("DINE-IN")
                .tableNo(1)
                .user(user)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();
        Order order2 = Order.builder()
                .id(2)
                .flavor("HAWAII")
                .crust("NORMAL")
                .size("L")
                .orderType("DINE-IN")
                .tableNo(2)
                .user(user)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();

        // when
        when(userService.doesUserExists(1)).thenReturn(true);
        when(orderRepository.getAllByUserId(1)).thenReturn(Optional.of(List.of(order1, order2)));
        when(orderDtoConverter.convert(order1)).thenReturn(orderResponse1);
        when(orderDtoConverter.convert(order2)).thenReturn(orderResponse2);

        // then
        List<OrderResponse> orderResponses = orderService.listOrdersOfUser(1);

        assertThat(orderResponses.get(0)).isEqualTo(orderResponse1);
        assertThat(orderResponses.get(1)).isEqualTo(orderResponse2);
    }

    @Test
    @DisplayName("It should throw exception when given user does not exists")
    void itShouldThrowResourceNotFoundExceptionWhenGivenUserDoesNotExists() {

        // given
        Integer userId = 1;

        // when
        when(userService.doesUserExists(userId)).thenReturn(false);

        // then
        assertThatThrownBy(() -> orderService.listOrdersOfUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("User with id = %d does not exists", userId));
    }

    @Test
    @DisplayName("It should cancel order when given order is exists")
    void itShouldCancelOrderWhenGivenOrderIsExists() {

        // given
        Order order1 = Order.builder()
                .id(1)
                .flavor("REGINA")
                .crust("THIN")
                .size("M")
                .orderType("DINE-IN")
                .tableNo(1)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();
        // when
        when(orderRepository.findById(order1.getId())).thenReturn(Optional.of(order1));

        // then
        orderService.cancelOrder(order1.getId());

        assertThat(order1.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED.getValue());
        verify(orderRepository).save(order1);
    }

    @Test
    @DisplayName("If order does not exists it should throw exception when cancelling the order")
    void ifOrderDoesNotExistsItShouldThrowResourceNotFoundExceptionWhenCancellingOrder() {

        // given
        Integer orderId = 1;

        // when
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Order with id = %d does not exists", orderId));
    }

    @Test
    @DisplayName("It should delete order when given order is exists")
    void itShouldDeleteOrderWhenGivenOrderIsExists() {

        // given
        Order order = Order.builder()
                .id(1)
                .flavor("REGINA")
                .crust("THIN")
                .size("M")
                .orderType("DINE-IN")
                .tableNo(1)
                .timestamp(Timestamp.from(NOW.toInstant()))
                .build();
        // when
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // then
        orderService.deleteOrder(order.getId());

        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("If order does not exists it should throw exception when deleting the order")
    void ifOrderDoesNotExistsItShouldThrowResourceNotFoundExceptionWhenDeletingOrder() {

        // given
        Integer orderId = 1;

        // when
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.format("Order with id = %d does not exists", orderId));
    }
}