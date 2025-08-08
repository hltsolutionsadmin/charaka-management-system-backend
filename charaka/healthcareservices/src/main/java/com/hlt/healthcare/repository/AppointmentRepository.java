package com.hlt.healthcare.repository;

import com.hlt.healthcare.model.AppointmentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentModel, Long> {
    Page<AppointmentModel> findByBusinessId(Long businessId, Pageable pageable);
    Page<AppointmentModel> findByDoctorId(Long doctorId, Pageable pageable);
}
