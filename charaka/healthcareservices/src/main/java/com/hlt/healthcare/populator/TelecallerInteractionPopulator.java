package com.hlt.healthcare.populator;

import com.hlt.healthcare.dto.TelecallerInteractionResponseDTO;
import com.hlt.healthcare.model.TelecallerInteractionModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TelecallerInteractionPopulator implements Populator<TelecallerInteractionModel, TelecallerInteractionResponseDTO> {

    @Override
    public void populate(TelecallerInteractionModel source, TelecallerInteractionResponseDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setCallerUserId(source.getCallerUserId());
        target.setTelecallerId(source.getTelecallerUserId());
        target.setBusinessId(source.getBusinessId());
        target.setInteractionType(source.getInteractionType());
        target.setNotes(source.getNotes());
        target.setCreatedAt(LocalDateTime.from(source.getCreatedAt()));
    }

    public TelecallerInteractionResponseDTO populateToResponseDTO(TelecallerInteractionModel saved) {
        TelecallerInteractionResponseDTO savedResponse = new TelecallerInteractionResponseDTO();
        populate(saved, savedResponse);
        return savedResponse;
    }
}
