package com.juvarya.delivery.repository;

import com.juvarya.delivery.model.DeliveryPartnerModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartnerModel, Long> {
    Page<DeliveryPartnerModel> findByActiveTrue(Pageable pageable);

    List<DeliveryPartnerModel> findByAvailableTrueOrderByLastAssignedTimeAsc();

    Optional<DeliveryPartnerModel> findByUserId(Long userId);

    Page<DeliveryPartnerModel> findByActiveTrueAndAvailableTrue(Pageable pageable);

    Optional<DeliveryPartnerModel> findByDeliveryPartnerId(String deliveryPartnerId);
}
