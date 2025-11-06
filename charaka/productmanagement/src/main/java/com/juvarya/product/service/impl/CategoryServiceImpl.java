package com.juvarya.product.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.product.azure.service.AwsBlobService;
import com.juvarya.product.dto.CategoryDTO;
import com.juvarya.product.dto.MediaDTO;
import com.juvarya.product.model.CategoryModel;
import com.juvarya.product.model.MediaModel;
import com.juvarya.product.repository.CategoryRepository;
import com.juvarya.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final AwsBlobService awsBlobService;

    @Override
    @Transactional
    public CategoryDTO addOrUpdateCategory(CategoryDTO dto) {
        String categoryName = dto.getName().trim();
        boolean isUpdate = dto.getId() != null;
        CategoryModel category;

        try {
            if (isUpdate) {

                category = categoryRepository.findById(dto.getId())
                        .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
                category.setName(categoryName);
                log.info("Updating Category with ID: {}", dto.getId());

                if (!CollectionUtils.isEmpty(category.getMedia())) {
                    category.getMedia().clear();
                }

            } else {
                categoryRepository.findByNameIgnoreCase(categoryName).ifPresent(existing -> {
                    throw new HltCustomerException(ErrorCode.CATEGORY_ALREADY_EXISTS);
                });
                category = new CategoryModel();
                category.setName(categoryName);
                log.info("Creating new Category: {}", categoryName);
            }

            List<MediaModel> newMedia = new ArrayList<>();
            if (!CollectionUtils.isEmpty(dto.getMediaFiles())) {
                newMedia = awsBlobService.uploadFiles(dto.getMediaFiles());
                newMedia.forEach(media -> media.setMediaType("CATEGORY"));
            }

            category.setMedia(newMedia);

            CategoryModel savedCategory = categoryRepository.save(category);

            CategoryDTO responseDto = modelMapper.map(savedCategory, CategoryDTO.class);

            if (!CollectionUtils.isEmpty(savedCategory.getMedia())) {
                List<MediaDTO> mediaDTOs = savedCategory.getMedia().stream()
                        .map(media -> {
                            MediaDTO dtoMedia = new MediaDTO();
                            dtoMedia.setMediaType(media.getMediaType());
                            dtoMedia.setUrl(media.getUrl());
                            return dtoMedia;
                        })
                        .collect(Collectors.toList());
                responseDto.setMedia(mediaDTOs);
            }

            return responseDto;

        } catch (IOException e) {
            log.error("Error processing media files", e);
            throw new HltCustomerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted Category with ID: {}", id);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> {
                    CategoryDTO dto = modelMapper.map(cat, CategoryDTO.class);

                    // Map media to DTOs
                    if (!CollectionUtils.isEmpty(cat.getMedia())) {
                        List<MediaDTO> mediaDTOs = cat.getMedia().stream()
                            .map(mediaModel -> {
                                MediaDTO mediaDTO = new MediaDTO();
                                mediaDTO.setMediaType(mediaModel.getMediaType());
                                mediaDTO.setUrl(mediaModel.getUrl());
                                return mediaDTO;
                            })
                            .collect(Collectors.toList());
                        dto.setMedia(mediaDTOs);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

}
