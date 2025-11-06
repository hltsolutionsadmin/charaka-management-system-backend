package com.hlt.productmanagement.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.productmanagement.dto.OfferDTO;
import com.hlt.productmanagement.dto.enums.OfferTargetType;
import com.hlt.productmanagement.model.OfferModel;
import com.hlt.productmanagement.populator.OfferPopulator;
import com.hlt.productmanagement.repository.OfferRepository;
import com.hlt.productmanagement.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferPopulator offerPopulator;

    @Override
    public OfferDTO saveOrUpdateOffer(OfferDTO dto) {
        validateOffer(dto);
        checkDuplicateCoupon(dto);

        boolean isCreate = (dto.getId() == null);
        OfferModel model;

        if (isCreate) {
            model = prepareNewOffer(dto);
            log.info("Creating new offer for businessId={} with name={}",
                    model.getBusinessId(), model.getName());
        } else {
            model = offerRepository.findById(dto.getId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.OFFER_NOT_FOUND));

            populateUpdateData(model, dto);
            log.info("Updating offer ID={} for businessId={} with name={}",
                    model.getId(), model.getBusinessId(), model.getName());
        }

        OfferModel saved = offerRepository.save(model);
        return offerPopulator.toDto(saved);
    }


    private void checkDuplicateCoupon(OfferDTO dto) {
        if (dto.getCouponCode() == null) {
            return;
        }

        offerRepository.findByCouponCode(dto.getCouponCode())
                .filter(existing -> dto.getId() == null || !existing.getId().equals(dto.getId()))
                .ifPresent(existing -> {
                    throw new HltCustomerException(ErrorCode.COUPON_CODE_ALREADY_EXISTS);
                });
    }


    @Override
    public OfferDTO getOfferById(Long id) {
        OfferModel model = offerRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.OFFER_NOT_FOUND));
        return offerPopulator.toDto(model);
    }

    @Override
    public Page<OfferDTO> getOffers(Long businessId, Boolean active, int page, int size) {
        if (businessId == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_ID_REQUIRED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<OfferModel> pageResult = offerRepository.findByBusinessIdAndActive(businessId, active, pageable);
        return pageResult.map(offerPopulator::toDto);
    }

    @Override
    public Page<OfferDTO> searchOffers(Long businessId, Boolean active, String keyword, int page, int size) {
        if (businessId == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_ID_REQUIRED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<OfferModel> result = offerRepository.searchOffers(businessId, active, keyword, pageable);
        return result.map(offerPopulator::toDto);
    }

    @Override
    public void deleteOffer(Long id) {
        OfferModel model = offerRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.OFFER_NOT_FOUND));
        model.setActive(false);
        offerRepository.save(model);
        log.info("Soft deleted offer with ID={}", id);
    }

    @Override
    public void expirePastOffers() {
        ZoneId zoneIST = ZoneId.of("Asia/Kolkata");
        LocalDateTime now = LocalDateTime.now(zoneIST);

        List<OfferModel> expiredOffers = offerRepository.findByActiveTrueAndEndDateBefore(now);

        for (OfferModel offer : expiredOffers) {
            offer.setActive(false);
            log.info("Auto-expired offer ID={} (ended at {} IST)", offer.getId(), offer.getEndDate());
        }

        if (!expiredOffers.isEmpty()) {
            offerRepository.saveAll(expiredOffers);
            log.info("Total {} offers auto-expired.", expiredOffers.size());
        } else {
            log.info("No offers to expire at {}", now);
        }
    }

    private OfferModel prepareNewOffer(OfferDTO dto) {
        OfferModel model = offerPopulator.toModel(dto);
        model.setActive(true);

        LocalDateTime now = LocalDateTime.now();
        model.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : now);
        model.setEndDate(dto.getEndDate() != null ? dto.getEndDate() : model.getStartDate().plusDays(30));

        if (dto.getTargetType() == null) {
            model.setTargetType(OfferTargetType.GLOBAL);
        } else {
            model.setTargetType(dto.getTargetType());
        }

        return model;
    }

    private void populateUpdateData(OfferModel model, OfferDTO dto) {
        model.setName(dto.getName());
        model.setOfferType(dto.getOfferType());
        model.setTargetType(dto.getTargetType() != null ? dto.getTargetType() : OfferTargetType.GLOBAL);
        model.setValue(dto.getValue());
        model.setMinOrderValue(dto.getMinOrderValue());
        model.setCouponCode(dto.getCouponCode());
        model.setStartDate(dto.getStartDate());
        model.setEndDate(dto.getEndDate());
        model.setBusinessId(dto.getBusinessId());
        model.setActive(dto.getActive());
        model.setDescription(dto.getDescription());
        model.setProductIds(dto.getProductIds());
        model.setCategoryIds(dto.getCategoryIds());
    }

    private void validateOffer(OfferDTO offer) {

        // Validate name
        if (isNullOrEmpty(offer.getName())) {
            throw new HltCustomerException(ErrorCode.OFFER_NAME_REQUIRED);
        }

        // Validate offer type
        if (offer.getOfferType() == null) {
            throw new HltCustomerException(ErrorCode.OFFER_TYPE_REQUIRED);
        }

        // Validate offer value
        if (offer.getValue() == null || offer.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HltCustomerException(ErrorCode.OFFER_VALUE_INVALID);
        }

        // Validate date range
        if (offer.getStartDate() != null && offer.getEndDate() != null
                && offer.getEndDate().isBefore(offer.getStartDate())) {
            throw new HltCustomerException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Validate business ID for non-global offers
        if (requiresBusinessId(offer)) {
            throw new HltCustomerException(ErrorCode.BUSINESS_ID_REQUIRED);
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean requiresBusinessId(OfferDTO offer) {
        return offer.getTargetType() != OfferTargetType.GLOBAL
                && offer.getTargetType() != OfferTargetType.LUCKY_ONE_RUPEE
                && offer.getBusinessId() == null;
    }

}
