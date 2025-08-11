package com.hlt.healthcare.repository;

import com.hlt.healthcare.model.PatientModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientModel, Long> {

    Optional<PatientModel> findByPatientCode(String patientCode);

    Optional<PatientModel> findByAppointment_Id(Long appointmentId);

    Page<PatientModel> findByBusinessId(Long businessId, Pageable pageable);
}
