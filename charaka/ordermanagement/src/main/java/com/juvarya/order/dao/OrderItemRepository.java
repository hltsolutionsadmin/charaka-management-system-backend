package com.juvarya.order.dao;

import com.juvarya.order.entity.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  OrderItemRepository extends JpaRepository<OrderModel, Long> {


}
