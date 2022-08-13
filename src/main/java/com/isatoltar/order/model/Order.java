package com.isatoltar.order.model;

import com.isatoltar.order.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column
    String flavor;

    @Column
    String crust;

    @Column
    String size;

    @Builder.Default
    @Column(name = "order_type", nullable = false)
    String orderType = "DINE-IN";

    @Builder.Default
    @Column(name = "order_status", nullable = false)
    Integer orderStatus = OrderStatus.CREATED.getValue();

    @Builder.Default
    @Column(name = "timestamp", nullable = false)
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    @Column(name = "table_no")
    Integer tableNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}