package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.healthcare.dto.TelecallerInteractionCreateDTO;
import com.hlt.healthcare.dto.TelecallerInteractionResponseDTO;
import com.hlt.healthcare.service.TelecallerInteractionService;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class TelecallerInteractionController {

    private final TelecallerInteractionService interactionService;

    @PostMapping
    public ResponseEntity<StandardResponse<TelecallerInteractionResponseDTO>> createInteraction(
            @RequestBody TelecallerInteractionCreateDTO dto) {
        UserDetailsImpl currentUser = SecurityUtils.getCurrentUserDetails();
        dto.setTelecallerId(currentUser.getId());

        TelecallerInteractionResponseDTO response = interactionService.createInteraction(dto);
        return ResponseEntity.ok(StandardResponse.single("Interaction created successfully", response));
    }

    @GetMapping("/telecaller/{telecallerId}")
    public ResponseEntity<StandardResponse<List<TelecallerInteractionResponseDTO>>> getInteractionsByTelecaller(
            @PathVariable Long telecallerId) {
        List<TelecallerInteractionResponseDTO> responseList = interactionService.getInteractionsByTelecaller(telecallerId);

        String message = responseList.isEmpty()
                ? "No interactions found for this telecaller"
                : "Telecaller interactions fetched successfully";

        return ResponseEntity.ok(StandardResponse.list(message, responseList));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<StandardResponse<List<TelecallerInteractionResponseDTO>>> getInteractionsByBusiness(
            @PathVariable Long businessId) {
        List<TelecallerInteractionResponseDTO> responseList = interactionService.getInteractionsByBusiness(businessId);

        String message = responseList.isEmpty()
                ? "No interactions found for this business"
                : "Business interactions fetched successfully";

        return ResponseEntity.ok(StandardResponse.list(message, responseList));
    }

}
