package com.juvarya.product.service;


import com.juvarya.product.model.MediaModel;

public interface MediaService {

    MediaModel saveMedia(MediaModel mediaModel);

    MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType);
}
