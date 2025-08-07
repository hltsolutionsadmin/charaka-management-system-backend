package com.hlt.healthcare.repository;

import com.hlt.healthcare.model.TelecallerInteractionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelecallerInteractionRepository extends JpaRepository<TelecallerInteractionModel, Long> {

    List<TelecallerInteractionModel> findByBusinessId(Long businessId);

    List<TelecallerInteractionModel> findByTelecallerUserId(Long telecallerId);
}
