package com.juvarya.user.access.mgmt.controllers;

import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.commonservice.dto.StandardResponse;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.MediaDTO;
import com.juvarya.user.access.mgmt.services.MediaService;
import com.juvarya.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/{b2bUnitId}")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN', 'ROLE_RESTAURANT_OWNER')")
    public StandardResponse<String>uploadMedia(@PathVariable Long b2bUnitId,
                                                @ModelAttribute MediaDTO mediaDTO) {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();

        boolean isUserAdmin = loggedInUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER_ADMIN"));

        boolean isRestaurantOwner = loggedInUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_RESTAURANT_OWNER"));

        if (!isUserAdmin && !isRestaurantOwner) {
            throw new JuvaryaCustomerException(ErrorCode.ACCESS_DENIED);
        }

        mediaService.uploadMedia(b2bUnitId, mediaDTO);
        return StandardResponse.message("Media uploaded successfully");
    }

    @GetMapping("/{b2bUnitId}/time-slot")
    public StandardResponse<List<MediaDTO>> getMediaForCurrentSlot(@PathVariable Long b2bUnitId) {
        List<MediaDTO> mediaList = mediaService.getMediaByTimeSlot(b2bUnitId);
        return StandardResponse.list("Fetched media for current slot", mediaList);
    }
}
