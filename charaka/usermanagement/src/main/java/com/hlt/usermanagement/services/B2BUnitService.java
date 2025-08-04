package com.hlt.usermanagement.services;


import com.hlt.usermanagement.dto.AddressDTO;
import com.hlt.usermanagement.dto.B2BUnitDTO;
import com.hlt.usermanagement.dto.B2BUnitStatusDTO;
import com.hlt.usermanagement.dto.request.B2BUnitRequest;
import com.hlt.usermanagement.dto.response.B2BUnitListResponse;
import com.hlt.usermanagement.model.B2BUnitModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface B2BUnitService {
    B2BUnitDTO createOrUpdate(B2BUnitRequest request) throws IOException;

    List<B2BUnitListResponse> listAll();

    List<B2BUnitDTO> getPendingApprovalList();

    void approveBusiness(Long businessId);

    B2BUnitDTO getById(Long id);

    List<B2BUnitDTO> getApprovedList();

    List<B2BUnitStatusDTO> getBusinessNameAndApprovalStatusForLoggedInUser();

    Page<B2BUnitModel> findB2BUnitsWithinRadius(double latitude, double longitude, double radiusInKm,
                                                String postalCode, String searchTerm, String categoryName, Pageable pageable);

    Page<B2BUnitDTO> getBusinessesByCategoryAndApproval(String categoryName, boolean approved, Pageable pageable);

    Page<B2BUnitDTO> searchByCityAndCategory(String city, String categoryName, String searchTerm, Pageable pageable);

    AddressDTO getAddressByB2BUnitId(Long unitId);



}
