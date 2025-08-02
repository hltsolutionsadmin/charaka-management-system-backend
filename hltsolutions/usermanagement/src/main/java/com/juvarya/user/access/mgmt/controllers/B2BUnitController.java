package com.juvarya.user.access.mgmt.controllers;

import com.juvarya.commonservice.dto.ApiResponse;
import com.juvarya.commonservice.dto.StandardResponse;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.*;
import com.juvarya.user.access.mgmt.dto.request.B2BUnitRequest;
import com.juvarya.user.access.mgmt.dto.response.B2BUnitListResponse;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import com.juvarya.user.access.mgmt.populator.B2BUnitPopulator;
import com.juvarya.user.access.mgmt.populator.UserPopulator;
import com.juvarya.user.access.mgmt.services.B2BUnitService;
import com.juvarya.user.access.mgmt.services.MediaService;
import com.juvarya.utils.JTBaseEndpoint;
import com.juvarya.utils.JuavaryaConstants;
import com.juvarya.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/business")
@Slf4j
public class B2BUnitController extends JTBaseEndpoint {

    @Autowired
    private B2BUnitService b2BUnitService;

    @Autowired
    private B2BUnitPopulator b2BUnitPopulator;

    @Autowired
    private  UserPopulator userPopulator;

    @Autowired
    private  MediaService mediaService;

    @PostMapping("/onboard")
    public ResponseEntity<B2BUnitDTO> createB2BUnit(@Valid @RequestBody B2BUnitRequest request) throws IOException {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be null");
        }
        B2BUnitDTO response = b2BUnitService.createOrUpdate(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<List<B2BUnitListResponse>> listBusinesses() {
        List<B2BUnitListResponse> businesses = b2BUnitService.listAll();
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<B2BUnitDTO>>> getApprovedBusinesses() {
        List<B2BUnitDTO> approvedList = b2BUnitService.getApprovedList();
        ApiResponse<List<B2BUnitDTO>> response = new ApiResponse<>(
                "Fetched approved businesses successfully",
                approvedList,
                approvedList.size()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize(JuavaryaConstants.ROLE_USER_ADMIN)
    public ResponseEntity<ApiResponse<List<B2BUnitDTO>>> getPendingBusinesses() {
        List<B2BUnitDTO> pendingList = b2BUnitService.getPendingApprovalList();
        ApiResponse<List<B2BUnitDTO>> response = new ApiResponse<>(
                "Fetched pending approval businesses successfully",
                pendingList,
                pendingList.size()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byCategory")
    @PreAuthorize(JuavaryaConstants.ROLE_USER_ADMIN)
    public ResponseEntity<ApiResponse<Page<B2BUnitDTO>>> getBusinessesByCategoryAndApproval(
            @RequestParam String categoryName,
            @RequestParam boolean approved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        Page<B2BUnitDTO> businessPage = b2BUnitService.getBusinessesByCategoryAndApproval(categoryName, approved, pageable);

        ApiResponse<Page<B2BUnitDTO>> response = new ApiResponse<>(
                "Fetched businesses successfully",
                businessPage,
                (int) businessPage.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }



    @GetMapping("/{id}")
    public ResponseEntity<B2BUnitDTO> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(b2BUnitService.getById(id));
    }

    @GetMapping("get")
    public ResponseEntity<B2BUnitDTO> getBusinessByToken() {
        log.info("Received request to fetch B2B unit details by token");
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        log.debug("Extracted user details: userId={}", loggedInUser.getId());
        B2BUnitDTO response = b2BUnitService.getById(loggedInUser.getId());
        log.info("Successfully retrieved B2B unit for userId={}", loggedInUser.getId());
        return ResponseEntity.ok(response);
    }


    @PutMapping("/approve/{businessId}")
    @PreAuthorize(JuavaryaConstants.ROLE_USER_ADMIN)
    public ResponseEntity<ApiResponse<String>> approveBusiness(@PathVariable Long businessId) {
        b2BUnitService.approveBusiness(businessId);
        ApiResponse<String> response = new ApiResponse<>(
                "Business approved successfully",
                "Business ID " + businessId + " is now approved.",
                1
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<List<B2BUnitStatusDTO>> getBusinessNameAndApprovalStatusForLoggedInUser() {
        List<B2BUnitStatusDTO> b2bUnitStatusList = b2BUnitService.getBusinessNameAndApprovalStatusForLoggedInUser();
        return ResponseEntity.ok(b2bUnitStatusList);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateBusinessStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        b2BUnitService.setBusinessEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        return ResponseEntity.ok("Business unit " + status + " successfully.");
    }

        @GetMapping("/find")
        public ResponseEntity<Page<B2BUnitDTO>> findNearbyUnits(
                @RequestParam(required = false) Double latitude,
                @RequestParam(required = false) Double longitude,
                @RequestParam String categoryName,
                @RequestParam(required = false, defaultValue = "10") double radius,
                @RequestParam(required = false) String postalCode,
                @RequestParam(required = false) String searchTerm,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {

            Pageable pageable = PageRequest.of(page, size);

            double latValue = latitude != null ? latitude : 0;
            double lngValue = longitude != null ? longitude : 0;

            Page<B2BUnitModel> unitPage = b2BUnitService.findB2BUnitsWithinRadius(latValue, lngValue, radius, postalCode,searchTerm, categoryName, pageable);

            if (unitPage.isEmpty()) {
                Page<B2BUnitDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
                return ResponseEntity.ok(emptyPage);
            }


            List<B2BUnitDTO> dtoList = unitPage.getContent().stream()
                    .map(unit -> {
                        B2BUnitDTO dto = new B2BUnitDTO();
                        b2BUnitPopulator.populate(unit, dto);
                        if (unit.getUserModel() != null) {
                            UserDTO userDTO = new UserDTO();
                            userPopulator.populate(unit.getUserModel(), userDTO, false);
                            dto.setUserDTO(userDTO);
                        }

                        List<MediaDTO> mediaList = mediaService.getMediaByTimeSlot(unit.getId());
                        dto.setMediaList(mediaList);
                        return dto;
                    }).toList();

            Page<B2BUnitDTO> dtoPage = new PageImpl<>(dtoList, pageable, unitPage.getTotalElements());

            return ResponseEntity.ok(dtoPage);
        }


    @GetMapping("/searchbycity")
    public ResponseEntity<StandardResponse<Page<B2BUnitDTO>>> searchByCity(
            @RequestParam String city,
            @RequestParam String categoryName,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Searching B2B units by city={}, categoryName={}, searchTerm={}", city, categoryName, searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        Page<B2BUnitDTO> result = b2BUnitService.searchByCityAndCategory(city, categoryName, searchTerm, pageable);
        return ResponseEntity.ok(StandardResponse.page("B2B units fetched successfully", result));
    }



    @GetMapping("/business/{id}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable("id") Long unitId) {
        AddressDTO addressDTO = b2BUnitService.getAddressByB2BUnitId(unitId);
        return ResponseEntity.ok(addressDTO);
    }

    @GetMapping("/business/{businessId}/verify-ip")
    public ResponseEntity<StandardResponse<String>> verifyIpAddress(
            @PathVariable Long businessId,
            @RequestParam String ipAddress) {
        boolean isValid = b2BUnitService.verifyIpAgainstBusiness(businessId, ipAddress);
        return ResponseEntity.ok(new StandardResponse<>("IP address verified successfully", "Success", null));
    }


}





