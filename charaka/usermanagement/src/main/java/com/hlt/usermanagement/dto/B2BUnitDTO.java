package com.hlt.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hlt.usermanagement.dto.response.ProductAttributeResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BUnitDTO {
	private Long id;

	private String businessName;

	private String contactNumber;

	private boolean approved;

	private boolean enabled;

	private Double businessLatitude;

	private Double businessLongitude;

	private String categoryName;

	private LocalDateTime creationDate;

	private UserDTO userDTO;

	private AddressDTO addressDTO;

	private Set<ProductAttributeResponse> attributes;

	private List<MultipartFile> mediaFiles;
	private List<String> mediaUrls;
	private List<MediaDTO> mediaList;


	public String getStatus() {
		return Boolean.TRUE.equals(enabled) ? "ENABLED" : "DISABLED";
	}
}
