package com.hlt.usermanagement.controllers;

import com.hlt.usermanagement.dto.*;
import com.hlt.commonservice.dto.ApiResponse;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.dto.request.B2BUnitRequest;
import com.hlt.usermanagement.dto.response.B2BUnitListResponse;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.populator.B2BUnitPopulator;
import com.hlt.usermanagement.populator.UserPopulator;
import com.hlt.usermanagement.services.B2BUnitService;
import com.hlt.usermanagement.services.MediaService;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
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

    @PostMapping("/onboard")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<B2BUnitDTO> createB2BUnit(@Valid @RequestBody B2BUnitRequest request) throws IOException {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be null");
        }
        B2BUnitDTO response = b2BUnitService.createOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<StandardResponse<Page<B2BUnitListResponse>>> listBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<B2BUnitListResponse> businesses = b2BUnitService.listAllPaginated(page, size);
        return ResponseEntity.ok(StandardResponse.page("Businesses fetched successfully", businesses));
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


    @GetMapping("/business/{id}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable("id") Long unitId) {
        AddressDTO addressDTO = b2BUnitService.getAddressByB2BUnitId(unitId);
        return ResponseEntity.ok(addressDTO);
    }


}





