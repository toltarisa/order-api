package com.isatoltar.order.repository;

import com.isatoltar.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<List<Order>> findAllByTableNoIn(Set<Integer> tableNumberSet);
}
