package com.hlt.healthcare.service;

import java.util.List;

public interface TelecallerInteractionService {

    TelecallerInteractionResponseDTO createInteraction(TelecallerInteractionCreateDTO dto);

    List<TelecallerInteractionResponseDTO> getInteractionsByTelecaller(Long telecallerId);

    List<TelecallerInteractionResponseDTO> getInteractionsByBusiness(Long businessId);
}
