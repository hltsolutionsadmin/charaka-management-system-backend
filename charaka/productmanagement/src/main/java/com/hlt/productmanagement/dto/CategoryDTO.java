package com.hlt.productmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    private Long id;
    private String name;

    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private List<MediaDTO> media;
}
