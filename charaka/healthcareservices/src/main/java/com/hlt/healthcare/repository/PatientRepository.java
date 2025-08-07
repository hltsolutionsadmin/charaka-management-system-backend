package com.hlt.healthcare.repository;

import com.hlt.healthcare.model.PatientModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientModel, Long> {
    Optional<PatientModel> findByEnquiryId(Long enquiryId);
    Page<PatientModel> findByBusinessId(Long businessId, Pageable pageable);
}
