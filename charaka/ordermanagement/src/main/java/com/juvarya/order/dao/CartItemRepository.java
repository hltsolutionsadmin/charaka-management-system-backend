package com.juvarya.order.dao;


import com.juvarya.order.entity.CartItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemModel, Long> {

    List<CartItemModel> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);

    Optional<CartItemModel> findByCartIdAndProductId(Long cartId, Long productId);
}
