package com.hlt.usermanagement.services;


import com.hlt.usermanagement.dto.MediaDTO;
import com.hlt.usermanagement.model.MediaModel;

public interface MediaService {

    MediaModel saveMedia(MediaModel mediaModel);

    MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType);

    void uploadMedia(Long b2bUnitId, MediaDTO dto);





}
