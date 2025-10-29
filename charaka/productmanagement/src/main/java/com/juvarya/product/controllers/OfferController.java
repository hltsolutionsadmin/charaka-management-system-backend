package com.juvarya.product.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.product.dto.OfferDTO;
import com.juvarya.product.service.OfferService;
import com.juvarya.product.utils.UserBusinessAccessHelper;
import com.hlt.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final UserBusinessAccessHelper userBusinessAccessHelper;

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN', 'ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<StandardResponse<OfferDTO>> saveOrUpdate(@Valid @RequestBody OfferDTO dto) {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();

         if (!hasRole(loggedInUser, "ROLE_USER_ADMIN")) {
             if (hasRole(loggedInUser, "ROLE_RESTAURANT_OWNER")) {
                 userBusinessAccessHelper.validateAccess(loggedInUser, dto.getBusinessId());
             }
         }

        OfferDTO savedOffer = offerService.saveOrUpdateOffer(dto);
        return ResponseEntity.ok(StandardResponse.single("Offer saved successfully", savedOffer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<OfferDTO>> getById(@PathVariable Long id) {
        OfferDTO offer = offerService.getOfferById(id);
        return ResponseEntity.ok(StandardResponse.single("Offer fetched successfully", offer));
    }

    @GetMapping("/list")
    public ResponseEntity<StandardResponse<Page<OfferDTO>>> listOffers(
            @RequestParam Long businessId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OfferDTO> offers = offerService.getOffers(businessId, active, page, size);
        return ResponseEntity.ok(StandardResponse.page("Offers listed successfully", offers));
    }

    @GetMapping("/search")
    public ResponseEntity<StandardResponse<Page<OfferDTO>>> searchOffers(
            @RequestParam Long businessId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OfferDTO> results = offerService.searchOffers(businessId, active, keyword, page, size);
        return ResponseEntity.ok(StandardResponse.page("Offer search result", results));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<String>> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.ok(StandardResponse.message("Offer deleted successfully"));
    }

    @PostMapping("/expire")
    public ResponseEntity<StandardResponse<String>> triggerExpirePastOffers() {
        offerService.expirePastOffers();
        return ResponseEntity.ok(StandardResponse.message("Expired offers deactivated"));
    }

    private boolean hasRole(UserDetailsImpl user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }
}
