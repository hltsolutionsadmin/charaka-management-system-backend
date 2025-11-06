package com.juvarya.product.service;

import com.juvarya.product.dto.OfferDTO;
import org.springframework.data.domain.Page;

public interface OfferService {

    OfferDTO saveOrUpdateOffer(OfferDTO dto);

    OfferDTO getOfferById(Long id);

    Page<OfferDTO> getOffers(Long businessId, Boolean active, int page, int size);

    void deleteOffer(Long id);

    Page<OfferDTO> searchOffers(Long businessId, Boolean active, String keyword, int page, int size);

    void expirePastOffers();


}
