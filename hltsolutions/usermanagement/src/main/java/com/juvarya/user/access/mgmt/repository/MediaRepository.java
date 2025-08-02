package com.juvarya.user.access.mgmt.repository;


import com.juvarya.user.access.mgmt.dto.enums.TimeSlot;
import com.juvarya.user.access.mgmt.model.MediaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<MediaModel, Long> {

    MediaModel findByCustomerIdAndMediaType(Long userId, String mediaType);
    List<MediaModel> findByCustomerId(Long userId);

    List<MediaModel> findByB2bUnitModelId(Long businessId);

    List<MediaModel> findByB2bUnitModelIdAndTimeSlotAndActiveTrue(Long businessId, TimeSlot timeSlot);


}
