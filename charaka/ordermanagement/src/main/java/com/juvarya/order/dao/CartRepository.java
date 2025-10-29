package com.juvarya.order.dao;
import com.juvarya.order.entity.CartModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartModel, Long> {

    Optional<CartModel> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT c FROM CartModel c WHERE c.status = 'UNORDERED' AND c.createdDate < :beforeDate")
    List<CartModel> findUnorderedCartsBefore(@Param("beforeDate") LocalDateTime beforeDate);

}
