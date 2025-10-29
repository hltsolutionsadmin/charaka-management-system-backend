package com.juvarya.product.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.product.dto.OfferDTO;
import com.juvarya.product.dto.enums.OfferTargetType;
import com.juvarya.product.model.OfferModel;
import com.juvarya.product.populator.OfferPopulator;
import com.juvarya.product.repository.OfferRepository;
import com.juvarya.product.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferPopulator offerPopulator;

    @Override
    public OfferDTO saveOrUpdateOffer(OfferDTO dto) {
        validateOffer(dto);
        if (dto.getCouponCode() != null) {
            Optional<OfferModel> existing = offerRepository.findByCouponCode(dto.getCouponCode());
            if (existing.isPresent() && (dto.getId() == null || !existing.get().getId().equals(dto.getId()))) {
                throw new HltCustomerException(ErrorCode.COUPON_CODE_ALREADY_EXISTS);
            }
        }
        OfferModel model;
        boolean isCreate = (dto.getId() == null);

        if (isCreate) {
            model = prepareNewOffer(dto);
            log.info("Creating new offer for businessId={} with name={}", model.getBusinessId(), model.getName());
        } else {
            model = offerRepository.findById(dto.getId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.OFFER_NOT_FOUND));
            populateUpdateData(model, dto);
            log.info("Updating offer ID={} for businessId={}", model.getId(), model.getBusinessId());
        }

        model = offerRepository.save(model);
        return offerPopulator.toDto(model);
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
        if (offer.getName() == null || offer.getName().trim().isEmpty()) {
            throw new HltCustomerException(ErrorCode.OFFER_NAME_REQUIRED);
        }

        if (offer.getOfferType() == null) {
            throw new HltCustomerException(ErrorCode.OFFER_TYPE_REQUIRED);
        }

        if (offer.getValue() == null || offer.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HltCustomerException(ErrorCode.OFFER_VALUE_INVALID);
        }

        if (offer.getStartDate() != null && offer.getEndDate() != null &&
                offer.getEndDate().isBefore(offer.getStartDate())) {
            throw new HltCustomerException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (offer.getTargetType() != OfferTargetType.GLOBAL && offer.getBusinessId() == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_ID_REQUIRED);
        }
    }
}
