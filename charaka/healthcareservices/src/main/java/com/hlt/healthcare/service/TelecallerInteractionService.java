package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.TelecallerInteractionCreateDTO;
import com.hlt.healthcare.dto.TelecallerInteractionResponseDTO;

import java.util.List;

public interface TelecallerInteractionService {

    TelecallerInteractionResponseDTO createInteraction(TelecallerInteractionCreateDTO dto);

    List<TelecallerInteractionResponseDTO> getInteractionsByTelecaller(Long telecallerId);

    List<TelecallerInteractionResponseDTO> getInteractionsByBusiness(Long businessId);
}
