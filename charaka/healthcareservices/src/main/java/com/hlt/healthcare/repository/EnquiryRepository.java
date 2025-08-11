package com.hlt.healthcare.repository;

import com.hlt.healthcare.model.EnquiryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnquiryRepository extends JpaRepository<EnquiryModel, Long> {
    Page<EnquiryModel> findByBusinessId(Long businessId, Pageable pageable);
    Page<EnquiryModel> findByTelecallerId(Long telecallerId, Pageable pageable);
    Page<EnquiryModel> findByProspectContact(String prospectContact, Pageable pageable);

    @Query("""
    SELECT DISTINCT e 
    FROM EnquiryModel e
    LEFT JOIN FETCH e.appointments a
    LEFT JOIN FETCH a.patient p
    WHERE e.prospectContactHash = :hash
""")
    List<EnquiryModel> findCustomerHistoryByContactHash(@Param("hash") String hash);
}
