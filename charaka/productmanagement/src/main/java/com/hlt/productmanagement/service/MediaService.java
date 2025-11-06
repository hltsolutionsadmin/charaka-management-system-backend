package com.hlt.productmanagement.service;


import com.hlt.productmanagement.model.MediaModel;

public interface MediaService {

    MediaModel saveMedia(MediaModel mediaModel);

    MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType);
}
