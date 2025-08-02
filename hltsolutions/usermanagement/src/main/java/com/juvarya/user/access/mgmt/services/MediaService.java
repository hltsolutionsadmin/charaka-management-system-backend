package com.juvarya.user.access.mgmt.services;


import com.juvarya.user.access.mgmt.dto.MediaDTO;
import com.juvarya.user.access.mgmt.model.B2BUnitModel;
import com.juvarya.user.access.mgmt.model.MediaModel;

import java.util.List;

public interface MediaService {

    MediaModel saveMedia(MediaModel mediaModel);

    MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType);

    void uploadMedia(Long b2bUnitId, MediaDTO dto);

    List<MediaDTO> getMediaByTimeSlot(Long b2bUnitId);

    B2BUnitModel getB2BUnitReference(Long b2bUnitId);



}
