package com.hlt.productmanagement.repository;



import com.hlt.productmanagement.model.MediaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends JpaRepository<MediaModel, Long> {
    MediaModel findByCustomerIdAndMediaType(Long userId, String mediaType);
}
