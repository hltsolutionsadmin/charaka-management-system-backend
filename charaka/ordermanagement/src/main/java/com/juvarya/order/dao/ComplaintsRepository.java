package com.juvarya.order.dao;

import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.entity.ComplaintsModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;

public interface ComplaintsRepository extends JpaRepository<ComplaintsModel, Long>, JpaSpecificationExecutor<ComplaintsModel> {

    Page<ComplaintsModel> findByB2bIdAndStatusIn(Long b2bId, List<ComplaintStatus> statuses, Pageable pageable);
    Page<ComplaintsModel> findByOrderIdAndStatusIn(Long orderId, List<ComplaintStatus> statuses, Pageable pageable);
    Page<ComplaintsModel> findByStatusIn(List<ComplaintStatus> statuses, Pageable pageable);
    Page<ComplaintsModel> findByCreatedByAndStatusIn(Long createdBy, List<ComplaintStatus> statuses, Pageable pageable);
    Page<ComplaintsModel> findByCreatedBy(Long createdBy, Pageable pageable);




}
