package com.hlt.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hlt.commonservice.dto.MediaDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BUnitRequest {

    @NotBlank(message = "Business name is mandatory")
    private String businessName;

    @NotNull(message = "Category ID is mandatory")
    private Long categoryId;

    @NotBlank(message = "Address Line 1 is mandatory")
    private String addressLine1;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String contactNumber;
    private Double latitude;
    private Double longitude;
    private Boolean enabled;

    private String gstNumber;
    private String fssaiNo;

    private List<ProductAttributeRequest> attributes;

    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private List<MediaDTO> media;


}
