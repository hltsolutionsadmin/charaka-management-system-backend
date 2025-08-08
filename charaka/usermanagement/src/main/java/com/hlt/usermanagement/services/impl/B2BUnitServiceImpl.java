package com.hlt.usermanagement.services.impl;

import com.hlt.usermanagement.azure.service.AwsBlobService;
import com.hlt.usermanagement.dto.AddressDTO;
import com.hlt.usermanagement.dto.B2BUnitDTO;
import com.hlt.usermanagement.dto.B2BUnitStatusDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.dto.request.B2BUnitRequest;
import com.hlt.usermanagement.dto.request.ProductAttributeRequest;
import com.hlt.usermanagement.dto.response.B2BUnitListResponse;
import com.hlt.usermanagement.dto.response.BusinessAttributeResponse;
import com.hlt.usermanagement.model.*;
import com.hlt.usermanagement.populator.AddressPopulator;
import com.hlt.usermanagement.populator.B2BUnitPopulator;
import com.hlt.usermanagement.populator.UserPopulator;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.BusinessCategoryRepository;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.B2BUnitService;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.commonservice.dto.Role;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class B2BUnitServiceImpl extends JTBaseEndpoint implements B2BUnitService {

    @Autowired
    private BusinessCategoryRepository categoryRepository;

    @Autowired
    private B2BUnitRepository b2bUnitRepository;

    @Autowired
    private B2BUnitPopulator b2bUnitPopulator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPopulator userPopulator;

    @Autowired
    private AddressPopulator addressPopulator;

    @Override
    @Transactional
    public B2BUnitDTO createOrUpdate(B2BUnitRequest request) throws IOException {
        UserModel currentUser = fetchCurrentUser();
        Optional<B2BUnitModel> existingModelOpt = b2bUnitRepository
                .findByOwnerAndBusinessNameIgnoreCase(currentUser, request.getBusinessName());

        B2BUnitModel unit = existingModelOpt.orElseGet(B2BUnitModel::new);
        unit.setOwner(currentUser);

        populateBasicDetails(unit, request, existingModelOpt);
        populateAddress(unit, request);
        populateCategory(unit, request);
        populateAttributes(unit, request);

        B2BUnitModel saved = b2bUnitRepository.save(unit);
        return buildResponseDTO(saved);
    }

    private UserModel fetchCurrentUser() {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
    }

    private void populateBasicDetails(B2BUnitModel unit, B2BUnitRequest request, Optional<B2BUnitModel> existingOpt) {
        if (request.getBusinessName() != null) unit.setBusinessName(request.getBusinessName());
        if (request.getContactNumber() != null) unit.setContactNumber(request.getContactNumber());
        if (request.getLatitude() != null) unit.setBusinessLatitude(request.getLatitude());
        if (request.getLongitude() != null) unit.setBusinessLongitude(request.getLongitude());
        // Uncomment below if you want to retain enabled status from existing record or default to true
        // unit.setEnabled(existingOpt.map(B2BUnitModel::isEnabled).orElse(true));
    }

    private void populateAddress(B2BUnitModel unit, B2BUnitRequest request) {
        AddressModel address = Optional.ofNullable(unit.getBusinessAddress())
                .orElse(new AddressModel());

        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getLatitude() != null) address.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) address.setLongitude(request.getLongitude());

        unit.setBusinessAddress(address);
    }

    private void populateCategory(B2BUnitModel unit, B2BUnitRequest request) {
        if (request.getCategoryId() != null) {
            BusinessCategoryModel category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
            unit.setCategory(category);
        }
    }

    private void populateAttributes(B2BUnitModel unit, B2BUnitRequest request) {
        if (request.getAttributes() == null || request.getAttributes().isEmpty()) return;

        Set<BusinessAttributeModel> attributes = Optional.ofNullable(unit.getAttributes())
                .orElseGet(() -> {
                    Set<BusinessAttributeModel> newSet = new HashSet<>();
                    unit.setAttributes(newSet);
                    return newSet;
                });

        attributes.clear();

        for (ProductAttributeRequest attr : request.getAttributes()) {
            BusinessAttributeModel model = new BusinessAttributeModel();
            model.setAttributeName(attr.getAttributeName());
            model.setAttributeValue(attr.getAttributeValue());
            model.setB2bUnitModel(unit);
            attributes.add(model);
        }
    }

    private B2BUnitDTO buildResponseDTO(B2BUnitModel savedModel) {
        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(savedModel, dto);

        if (savedModel.getOwner() != null) {
            UserDTO userDTO = new UserDTO();
            userPopulator.populate(savedModel.getOwner(), userDTO, false);
            dto.setUserDTO(userDTO);
        }

        return dto;
    }

    @Override
    public Page<B2BUnitListResponse> listAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        Page<B2BUnitModel> b2bUnits = b2bUnitRepository.findAll(pageable);

        return b2bUnits.map(this::mapToB2BUnitListResponse);
    }

    private B2BUnitListResponse mapToB2BUnitListResponse(B2BUnitModel model) {
        B2BUnitListResponse response = new B2BUnitListResponse();

        response.setId(model.getId());
        response.setBusinessName(model.getBusinessName());
        response.setEnabled(model.isEnabled());
        response.setCreationDate(model.getCreationDate());

        if (model.getCategory() != null) {
            response.setCategoryName(model.getCategory().getName());
        }

        if (model.getOwner() != null) {
            response.setUserId(model.getOwner().getId());
        }

        if (model.getAttributes() != null && !model.getAttributes().isEmpty()) {
            Set<BusinessAttributeResponse> attributes = model.getAttributes().stream()
                    .map(attr -> new BusinessAttributeResponse(
                            attr.getId(),
                            attr.getAttributeName(),
                            attr.getAttributeValue()))
                    .collect(Collectors.toSet());
            response.setAttributes(attributes);
        }

        return response;
    }

    private B2BUnitDTO convertToDTO(B2BUnitModel model) {
        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(model, dto);
        return dto;
    }

    @Override
    public B2BUnitDTO getById(Long id) {
        B2BUnitModel model = b2bUnitRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(model, dto);
        dto.setEnabled(Boolean.TRUE.equals(model.isEnabled()));

        return dto;
    }

    private B2BUnitStatusDTO mapToStatusDTO(B2BUnitModel b2BUnit, Set<Role> userRoles) {
        Set<BusinessAttributeResponse> attributes = b2BUnit.getAttributes().stream()
                .map(attr -> new BusinessAttributeResponse(
                        attr.getId(),
                        attr.getAttributeName(),
                        attr.getAttributeValue()
                ))
                .collect(Collectors.toSet());

        return new B2BUnitStatusDTO(
                b2BUnit.getId(),
                b2BUnit.getBusinessName(),
                b2BUnit.isEnabled(),
                userRoles,
                attributes
        );
    }

    @Override
    public AddressDTO getAddressByB2BUnitId(Long unitId) {
        AddressModel addressModel = b2bUnitRepository.findBusinessAddressByUnitId(unitId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found for B2B Unit ID: " + unitId));
        AddressDTO addressDTO = new AddressDTO();
        addressPopulator.populate(addressModel, addressDTO);
        return addressDTO;
    }

}
