package com.hlt.productmanagement.populator;


import com.hlt.commonservice.dto.MediaDTO;
import com.hlt.productmanagement.model.MediaModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class MediaPopulator implements Populator<MediaModel, MediaDTO> {

    @Override
    public void populate(MediaModel source, MediaDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setUrl(source.getUrl());
        target.setDescription(source.getDescription());
        target.setExtension(source.getExtension());

    }

}
