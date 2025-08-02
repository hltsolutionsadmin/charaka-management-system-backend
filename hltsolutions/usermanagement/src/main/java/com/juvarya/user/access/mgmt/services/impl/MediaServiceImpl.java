package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.user.access.mgmt.azure.service.AwsBlobService;
import com.juvarya.user.access.mgmt.dto.MediaDTO;
import com.juvarya.user.access.mgmt.dto.enums.TimeSlot;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import com.juvarya.user.access.mgmt.model.MediaModel;
import com.juvarya.user.access.mgmt.populator.MediaPopulator;
import com.juvarya.user.access.mgmt.repository.B2BUnitRepository;
import com.juvarya.user.access.mgmt.repository.MediaRepository;
import com.juvarya.user.access.mgmt.services.MediaService;
import com.juvarya.user.access.mgmt.utils.TimeSlotResolverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final B2BUnitRepository b2bUnitRepository;
    private final MediaPopulator mediaPopulator;
    private final TimeSlotResolverService timeSlotResolverService;

    @Lazy
    private final AwsBlobService awsBlobService;

    @Override
    @Transactional
    public MediaModel saveMedia(MediaModel mediaModel) {
        return mediaRepository.save(mediaModel);
    }

    @Override
    public MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType) {
        return mediaRepository.findByCustomerIdAndMediaType(userId, mediaType);
    }

    @Override
    public void uploadMedia(Long b2bUnitId, MediaDTO dto) {
        B2BUnitModel b2b = b2bUnitRepository.findById(b2bUnitId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        if (!CollectionUtils.isEmpty(dto.getMediaFiles())) {
            uploadMediaFilesToAws(dto, b2b); // Upload files to S3 and save
        } else {
            // Manual save when only external URL is given
            TimeSlot timeSlot = resolveTimeSlot(dto.getTimeSlot());

            MediaModel media = new MediaModel();
            media.setB2bUnitModel(b2b);
            media.setUrl(dto.getUrl());
            media.setTimeSlot(timeSlot);
            media.setFileName(dto.getFileName());
            media.setMediaType(dto.getMediaType());
            media.setDescription(dto.getDescription());
            media.setExtension(dto.getExtension());
            media.setActive(dto.isActive());
            media.setCreatedBy(dto.getCreatedBy());
            media.setCustomerId(dto.getCustomerId());

            mediaRepository.save(media);
        }
    }

    private List<MediaModel> uploadMediaFilesToAws(MediaDTO dto, B2BUnitModel b2b) {
        TimeSlot timeSlot = resolveTimeSlot(dto.getTimeSlot());
        List<MediaModel> uploaded = new ArrayList<>();

        for (MultipartFile file : dto.getMediaFiles()) {
            if (file == null || file.isEmpty()) {
                throw new JuvaryaCustomerException(ErrorCode.INVALID_INPUT, "Uploaded file is empty or missing");
            }

            try {
                MediaModel media = awsBlobService.uploadB2BMedia(b2b.getId(), file, timeSlot, dto.getCreatedBy());

                // Set extra fields
                media.setMediaType(dto.getMediaType());
                media.setDescription(dto.getDescription());
                media.setCustomerId(dto.getCustomerId());
                media.setActive(true);

                mediaRepository.save(media);
                uploaded.add(media);
            } catch (IOException e) {
                throw new JuvaryaCustomerException(ErrorCode.MEDIA_UPLOAD_FAILED, "AWS S3 file upload failed");
            }
        }

        return uploaded;
    }

    private TimeSlot resolveTimeSlot(String slotStr) {
        try {
            return TimeSlot.valueOf(slotStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new JuvaryaCustomerException(ErrorCode.INVALID_INPUT, "Invalid time slot: " + slotStr);
        }
    }

    @Override
    public List<MediaDTO> getMediaByTimeSlot(Long b2bUnitId) {
        TimeSlot currentSlot = timeSlotResolverService.resolveCurrentSlot();

        return mediaRepository.findByB2bUnitModelIdAndTimeSlotAndActiveTrue(b2bUnitId, currentSlot)
                .stream()
                .map(media -> {
                    MediaDTO dto = new MediaDTO();
                    mediaPopulator.populate(media, dto);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public B2BUnitModel getB2BUnitReference(Long b2bUnitId) {
        return b2bUnitRepository.getReferenceById(b2bUnitId);
    }
}
