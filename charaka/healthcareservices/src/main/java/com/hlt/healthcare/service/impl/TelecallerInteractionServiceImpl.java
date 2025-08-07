package com.hlt.healthcare.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.Role;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;
import com.hlt.healthcare.client.UserMgmtClient;
import com.hlt.healthcare.service.TelecallerInteractionService;
import com.hlt.utils.SecurityUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelecallerInteractionServiceImpl implements TelecallerInteractionService {

    private final TelecallerInteractionRepository repository;
    private final TelecallerInteractionPopulator populator;
    private final UserMgmtClient userMgmtClient;

    @Override
    public TelecallerInteractionResponseDTO createInteraction(TelecallerInteractionCreateDTO dto) {
        UserDTO user = getOrCreateUserByMobile(dto.getMobileNumber());

        TelecallerInteractionModel model = TelecallerInteractionModel.builder()
                .telecallerUserId(SecurityUtils.getCurrentUserDetails().getId())
                .callerUserId(user.getId())
                .interactionType(dto.getInteractionType())
                .notes(dto.getNotes())
                .businessId(dto.getBusinessId())
                .build();

        TelecallerInteractionModel saved = repository.save(model);

        return populator.populateToResponseDTO(saved);
    }

    @Override
    public List<TelecallerInteractionResponseDTO> getInteractionsByTelecaller(Long telecallerId) {
        return repository.findByTelecallerUserId(telecallerId).stream()
                .map(populator::populateToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TelecallerInteractionResponseDTO> getInteractionsByBusiness(Long businessId) {
        return repository.findByBusinessId(businessId).stream()
                .map(populator::populateToResponseDTO)
                .collect(Collectors.toList());
    }

    private UserDTO getOrCreateUserByMobile(String mobile) {
        try {
            log.info("Fetching user by contact: {}", mobile);
            return userMgmtClient.getUserByPrimaryContact(mobile);

        } catch (FeignException.NotFound e) {
            log.warn("User not found by mobile: {}, creating new user", mobile);
            return createUserFromMobile(mobile);
        } catch (FeignException e) {
            log.error("Error while fetching user by contact: {}", mobile, e);
            throw new HltCustomerException(ErrorCode.INTERNAL_SERVER_ERROR, "Error fetching user by contact");
        }
    }

    private UserDTO createUserFromMobile(String mobile) {
        log.info("Creating new user with contact: {}", mobile);

        Role defaultRole = new Role();
        defaultRole.setName(ERole.ROLE_USER);

        UserDTO request = UserDTO.builder()
                .primaryContact(mobile)
                .roles(Set.of(defaultRole))
                .build();

        return userMgmtClient.saveUser(request);
    }
}
