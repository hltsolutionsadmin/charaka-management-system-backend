package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.commonservice.dto.RequestContext;
import com.juvarya.commonservice.dto.Role;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.azure.service.AwsBlobService;
import com.juvarya.user.access.mgmt.dto.AddressDTO;
import com.juvarya.user.access.mgmt.dto.B2BUnitDTO;


import com.juvarya.user.access.mgmt.dto.B2BUnitStatusDTO;
import com.juvarya.user.access.mgmt.dto.UserDTO;
import com.juvarya.user.access.mgmt.dto.enums.EnabledStatusSource;
import com.juvarya.user.access.mgmt.dto.response.B2BUnitListResponse;
import com.juvarya.user.access.mgmt.dto.response.ProductAttributeResponse;
import com.juvarya.user.access.mgmt.dto.request.B2BUnitRequest;
import com.juvarya.user.access.mgmt.dto.request.ProductAttributeRequest;
import com.juvarya.user.access.mgmt.model.*;
import com.juvarya.user.access.mgmt.populator.AddressPopulator;
import com.juvarya.user.access.mgmt.populator.B2BUnitPopulator;
import com.juvarya.user.access.mgmt.populator.UserPopulator;
import com.juvarya.user.access.mgmt.repository.ApiKeyRepository;
import com.juvarya.user.access.mgmt.repository.BusinessCategoryRepository;
import com.juvarya.user.access.mgmt.repository.B2BUnitRepository;
import com.juvarya.user.access.mgmt.repository.UserRepository;
import com.juvarya.user.access.mgmt.services.B2BUnitService;
import com.juvarya.user.access.mgmt.utils.BusinessTimingUtil;
import com.juvarya.utils.JTBaseEndpoint;
import com.juvarya.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private AwsBlobService awsBlobService;

    @Autowired
    private AddressPopulator addressPopulator;

    @Override
    @Transactional
    public B2BUnitDTO createOrUpdate(B2BUnitRequest request) throws IOException {
        Long apiKeyId = RequestContext.getApiKeyId();

        UserModel currentUser = resolveCurrentUser(apiKeyId);
        Optional<B2BUnitModel> existingModelOpt = b2bUnitRepository
                .findByUserModelAndBusinessNameIgnoreCase(currentUser, request.getBusinessName());

        B2BUnitModel unit = existingModelOpt.orElseGet(B2BUnitModel::new);
        unit.setUserModel(currentUser);
        populateBasicDetails(unit, request, existingModelOpt);
        populateAddress(unit, request);
        populateCategory(unit, request);
        populateAttributes(unit, request);
        unit.setEnabled(BusinessTimingUtil.isBusinessEnabled(unit.getAttributes()));

        B2BUnitModel saved = b2bUnitRepository.save(unit);
        return buildResponseDTO(saved);
    }
    private UserModel resolveCurrentUser(Long apiKeyId) {
        if (apiKeyId != null) {
            ApiKeyModel apiKey = apiKeyRepository.findById(apiKeyId)
                    .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.UNAUTHORIZED_API_KEY));
            return apiKey.getUser();
        } else {
            UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.USER_NOT_FOUND));
        }
    }

    private void populateBasicDetails(B2BUnitModel unit, B2BUnitRequest request, Optional<B2BUnitModel> existingOpt) {
        if (request.getBusinessName() != null) unit.setBusinessName(request.getBusinessName());
        if (request.getContactNumber() != null) unit.setContactNumber(request.getContactNumber());
        if (request.getLatitude() != null) unit.setBusinessLatitude(request.getLatitude());
        if (request.getLongitude() != null) unit.setBusinessLongitude(request.getLongitude());
        unit.setApproved(existingOpt.map(B2BUnitModel::isApproved).orElse(false));
    }

    private void populateAddress(B2BUnitModel unit, B2BUnitRequest request) {
        AddressModel address = Optional.ofNullable(unit.getBusinessAddress()).orElse(new AddressModel());

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
                    .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
            unit.setCategory(category);
        }
    }
    private void populateAttributes(B2BUnitModel unit, B2BUnitRequest request) {
        if (request.getAttributes() == null || request.getAttributes().isEmpty()) return;

        Set<ProductAttributeModel> attributes = Optional.ofNullable(unit.getAttributes())
                .orElseGet(() -> {
                    Set<ProductAttributeModel> newSet = new HashSet<>();
                    unit.setAttributes(newSet);
                    return newSet;
                });

        attributes.clear();

        for (ProductAttributeRequest attr : request.getAttributes()) {
            ProductAttributeModel model = new ProductAttributeModel();
            model.setAttributeName(attr.getAttributeName());
            model.setAttributeValue(attr.getAttributeValue());
            model.setB2bUnitModel(unit);
            attributes.add(model);
        }
    }
    private B2BUnitDTO buildResponseDTO(B2BUnitModel savedModel) {
        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(savedModel, dto);

        if (savedModel.getUserModel() != null) {
            UserDTO userDTO = new UserDTO();
            userPopulator.populate(savedModel.getUserModel(), userDTO, false);
            dto.setUserDTO(userDTO);
        }

        return dto;
    }

    @Override
    public List<B2BUnitListResponse> listAll() {
        return b2bUnitRepository.findAll().stream()
                .map(this::mapToB2BUnitListResponse)
                .collect(Collectors.toList());
    }
    private B2BUnitListResponse mapToB2BUnitListResponse(B2BUnitModel model) {
        B2BUnitListResponse response = new B2BUnitListResponse();

        response.setId(model.getId());
        response.setBusinessName(model.getBusinessName());
        response.setEnabled(model.isEnabled());
        response.setApproved(model.isApproved());
        response.setCreationDate(model.getCreationDate());

        if (model.getCategory() != null) {
            response.setCategoryName(model.getCategory().getName());
        }

        if (model.getUserModel() != null) {
            response.setUserId(model.getUserModel().getId());
        }

        if (model.getAttributes() != null && !model.getAttributes().isEmpty()) {
            Set<ProductAttributeResponse> attributes = model.getAttributes().stream()
                    .map(attr -> new ProductAttributeResponse(
                            attr.getId(),
                            attr.getAttributeName(),
                            attr.getAttributeValue()))
                    .collect(Collectors.toSet());
            response.setAttributes(attributes);
        }

        return response;
    }

    @Override
    public List<B2BUnitDTO> getPendingApprovalList() {
        return b2bUnitRepository.findByApprovedFalseOrderByCreationDateDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<B2BUnitDTO> getApprovedList() {
        return b2bUnitRepository.findByApprovedTrueOrderByCreationDateDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    private B2BUnitDTO convertToDTO(B2BUnitModel model) {
        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(model, dto);
        return dto;
    }

    @Override
    public void approveBusiness(Long businessId) {
        B2BUnitModel business = b2bUnitRepository.findById(businessId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        if (Boolean.TRUE.equals(business.isApproved())) {
            throw new JuvaryaCustomerException(ErrorCode.ALREADY_APPROVED);
        }

        business.setApproved(true);
        b2bUnitRepository.save(business);
    }

    @Override
    public B2BUnitDTO getById(Long id) {
        B2BUnitModel model = b2bUnitRepository.findById(id)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        B2BUnitDTO dto = new B2BUnitDTO();
        b2bUnitPopulator.populate(model, dto);
        dto.setEnabled(Boolean.TRUE.equals(model.isEnabled()));

        return dto;
    }


    @Override
    public List<B2BUnitStatusDTO> getBusinessNameAndApprovalStatusForLoggedInUser() {
        Long userId = SecurityUtils.getCurrentUserDetails().getId();

        UserModel userModel = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<Role> userRoles = userModel.getRoleModels().stream()
                .map(role -> new Role(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        List<B2BUnitModel> b2BUnits = b2bUnitRepository.findByUserModelId(userId);

        if (b2BUnits.isEmpty()) {
            return List.of(B2BUnitStatusDTO.rolesOnly(userRoles));
        }

        return b2BUnits.stream()
                .map(b2BUnit -> mapToStatusDTO(b2BUnit, userRoles))
                .collect(Collectors.toList());
    }
    private B2BUnitStatusDTO mapToStatusDTO(B2BUnitModel b2BUnit, Set<Role> userRoles) {
        Set<ProductAttributeResponse> attributes = b2BUnit.getAttributes().stream()
                .map(attr -> new ProductAttributeResponse(
                        attr.getId(),
                        attr.getAttributeName(),
                        attr.getAttributeValue()
                ))
                .collect(Collectors.toSet());

        return new B2BUnitStatusDTO(
                b2BUnit.getId(),
                b2BUnit.getBusinessName(),
                b2BUnit.isEnabled(),
                b2BUnit.isApproved(),
                userRoles,
                attributes
        );
    }

    @Override
    @Transactional
    public String setBusinessEnabledStatus(Long id, boolean enabled) {
        B2BUnitModel entity = b2bUnitRepository.findById(id)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        entity.setEnabled(enabled);
        entity.setEnabledStatusSource(enabled ? EnabledStatusSource.MANUAL : EnabledStatusSource.SCHEDULER);

        b2bUnitRepository.saveAndFlush(entity);

        return String.format("Business unit %s successfully.", enabled ? "enabled" : "disabled");
    }

    @Override
    public Page<B2BUnitModel> findB2BUnitsWithinRadius(
            double latitude,
            double longitude,
            double radiusInKm,
            String postalCode,
            String searchTerm,
            String categoryName,
            Pageable pageable) {

        Page<B2BUnitModel> resultsPage = Page.empty(pageable);
        boolean hasLatLng = latitude != 0 && longitude != 0;
        boolean hasPostalCode = postalCode != null && !postalCode.isBlank();
        boolean hasSearchTerm = searchTerm != null && !searchTerm.isBlank();

        if (hasLatLng) {
            resultsPage = b2bUnitRepository.findNearbyBusinessesWithCategoryFilter(latitude, longitude, radiusInKm, categoryName, pageable);
        } else if (hasPostalCode) {
            resultsPage = b2bUnitRepository.findByUserAddressPostalCode(postalCode, pageable);
        }

        if (hasSearchTerm && !resultsPage.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            List<B2BUnitModel> filteredList = resultsPage.stream()
                    .filter(unit -> unit.getBusinessName() != null &&
                            unit.getBusinessName().toLowerCase().contains(lowerSearchTerm))
                    .collect(Collectors.toList());

            return new PageImpl<>(filteredList, pageable, filteredList.size());
        }

        return resultsPage;
    }
    public Page<B2BUnitDTO> getBusinessesByCategoryAndApproval(String categoryName, boolean approved, Pageable pageable) {
        Page<B2BUnitModel> page = b2bUnitRepository.findByCategory_NameAndApprovedOrderByCreationDateDesc(categoryName, approved, pageable);
        return page.map(business -> {
            B2BUnitDTO dto = new B2BUnitDTO();
            b2bUnitPopulator.populate(business, dto);
            return dto;
        });
    }

    @Override
    public Page<B2BUnitDTO> searchByCityAndCategory(String city, String categoryName, String searchTerm, Pageable pageable) {
        Page<B2BUnitModel> modelPage = b2bUnitRepository.findByCityAndCategoryName(city, categoryName, pageable);

        List<B2BUnitModel> filteredModels = modelPage.stream()
                .filter(model -> {
                    if (searchTerm == null || searchTerm.isBlank()) return true;
                    String businessName = model.getBusinessName();
                    return businessName != null && businessName.toLowerCase().contains(searchTerm.toLowerCase());
                })
                .collect(Collectors.toList());

        List<B2BUnitDTO> dtoList = filteredModels.stream()
                .map(model -> {
                    B2BUnitDTO dto = new B2BUnitDTO();
                    b2bUnitPopulator.populate(model, dto);
                    if (model.getUserModel() != null) {
                        UserDTO userDTO = new UserDTO();
                        userPopulator.populate(model.getUserModel(), userDTO, false);
                        dto.setUserDTO(userDTO);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, filteredModels.size());
    }

    public AddressDTO getAddressByB2BUnitId(Long unitId) {
        AddressModel addressModel = b2bUnitRepository.findBusinessAddressByUnitId(unitId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.ADDRESS_NOT_FOUND, "Address not found for B2B Unit ID: " + unitId));
        AddressDTO addressDTO = new AddressDTO();
        addressPopulator.populate(addressModel, addressDTO);
        return addressDTO;
    }

    @Override
    public boolean verifyIpAgainstBusiness(Long businessId, String ipAddress) {
        B2BUnitModel business = b2bUnitRepository.findById(businessId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        String storedIp = business.getAttributes().stream()
                .filter(attr -> "Ip Address".equalsIgnoreCase(attr.getAttributeName()))
                .map(ProductAttributeModel::getAttributeValue)
                .findFirst()
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.INVALID_IP_ADDRESS));

        if (!storedIp.equals(ipAddress)) {
            throw new JuvaryaCustomerException(ErrorCode.IP_ADDRESS_MISMATCH);
        }

        return true;
    }
}