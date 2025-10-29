package com.juvarya.order.utils;

import com.juvarya.order.dto.enums.OrderStatus;
import com.juvarya.order.entity.OrderModel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OrderSpecification {

    public static Specification<OrderModel> filter(
            Long userId,
            Long businessId,
            String orderStatus,
            LocalDate fromDate,
            LocalDate toDate,
            String orderNumber
    ) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
            }

            if (businessId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("businessId"), businessId));
            }

            if (orderStatus != null && !orderStatus.isBlank()) {
                try {
                    OrderStatus statusEnum = OrderStatus.valueOf(orderStatus.trim().toUpperCase());
                    predicate = cb.and(predicate, cb.equal(root.get("orderStatus"), statusEnum));
                } catch (IllegalArgumentException ex) {
                    // Optional: log or skip invalid order status
                    // log.warn("Invalid order status: {}", orderStatus);
                }
            }

            if (orderNumber != null && !orderNumber.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("orderNumber"), "%" + orderNumber.trim() + "%"));
            }

            if (fromDate != null) {
                LocalDateTime startDateTime = fromDate.atStartOfDay();
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdDate"), startDateTime));
            }

            if (toDate != null) {
                LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdDate"), endDateTime));
            }

            return predicate;
        };
    }
}
