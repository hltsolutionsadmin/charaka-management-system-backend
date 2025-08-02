package com.juvarya.user.access.mgmt.controllers;

import com.juvarya.user.access.mgmt.dto.B2BUnitDTO;
import com.juvarya.user.access.mgmt.dto.UserDTO;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import com.juvarya.user.access.mgmt.populator.B2BUnitPopulator;
import com.juvarya.user.access.mgmt.populator.UserPopulator;
import com.juvarya.user.access.mgmt.services.B2BUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicB2BUnitController {

    private final B2BUnitService b2BUnitService;
    private final B2BUnitPopulator b2BUnitPopulator;
    private final UserPopulator userPopulator;

    @GetMapping("/find")
    public ResponseEntity<Page<B2BUnitDTO>> findNearbyUnits(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") double radius,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        double latValue = latitude != null ? latitude : 0;
        double lngValue = longitude != null ? longitude : 0;

        Page<B2BUnitModel> unitPage = b2BUnitService.findB2BUnitsWithinRadius(
                latValue, lngValue, radius, postalCode, searchTerm,categoryName,pageable);

        if (unitPage.isEmpty()) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList(), pageable, 0));
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
                    return dto;
                }).toList();

        Page<B2BUnitDTO> dtoPage = new PageImpl<>(dtoList, pageable, unitPage.getTotalElements());

        return ResponseEntity.ok(dtoPage);
    }
}
