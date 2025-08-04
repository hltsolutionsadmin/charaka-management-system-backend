package com.hlt.usermanagement.repository;


import com.hlt.usermanagement.model.MediaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<MediaModel, Long> {

    MediaModel findByCustomerIdAndMediaType(Long userId, String mediaType);
    List<MediaModel> findByCustomerId(Long userId);

    List<MediaModel> findByB2bUnitModelId(Long businessId);

}
