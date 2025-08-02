package com.juvarya.user.access.mgmt.services;


import com.juvarya.user.access.mgmt.dto.AddressDTO;
import com.juvarya.user.access.mgmt.dto.B2BUnitDTO;
import com.juvarya.user.access.mgmt.dto.B2BUnitStatusDTO;
import com.juvarya.user.access.mgmt.dto.request.B2BUnitRequest;
import com.juvarya.user.access.mgmt.dto.response.B2BUnitListResponse;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import jakarta.servlet.http.HttpServletRequest;
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

    String setBusinessEnabledStatus(Long id, boolean enabled);

    Page<B2BUnitModel> findB2BUnitsWithinRadius(double latitude, double longitude, double radiusInKm,
                                                String postalCode, String searchTerm,String categoryName,Pageable pageable);

    Page<B2BUnitDTO> getBusinessesByCategoryAndApproval(String categoryName, boolean approved, Pageable pageable);

     Page<B2BUnitDTO> searchByCityAndCategory(String city, String categoryName, String searchTerm, Pageable pageable) ;

    AddressDTO getAddressByB2BUnitId(Long unitId);

     boolean verifyIpAgainstBusiness(Long businessId, String ipAddress);



}
