package com.juvarya.product.service.impl;



import com.juvarya.product.model.MediaModel;
import com.juvarya.product.repository.MediaRepository;
import com.juvarya.product.service.MediaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    @Override
    @Transactional
    public MediaModel saveMedia(MediaModel mediaModel) {

        return mediaRepository.save(mediaModel);
    }

    @Override
    public MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType) {
        return mediaRepository.findByCustomerIdAndMediaType(userId, mediaType);
    }
}
