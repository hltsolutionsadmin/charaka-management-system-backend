package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.AddressDTO;
import com.hlt.usermanagement.dto.B2BUnitDTO;
import com.hlt.usermanagement.dto.response.BusinessAttributeResponse;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.utils.Populator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class B2BUnitPopulator implements Populator<B2BUnitModel, B2BUnitDTO> {

	@Autowired
	private UserPopulator userPopulator;

	@Autowired
	private AddressPopulator addressPopulator;

	@Autowired
	private BusinessAttributePopulator attributePopulator;

	public B2BUnitDTO toDTO(B2BUnitModel source) {
		if (source == null)
			return null;
		B2BUnitDTO dto = new B2BUnitDTO();
		populate(source, dto);
		return dto;
	}

	@Override
	public void populate(B2BUnitModel source, B2BUnitDTO target) {
		if (source == null) {
			return;
		}

		target.setId(source.getId());
		target.setBusinessName(source.getBusinessName());
		target.setContactNumber(source.getContactNumber());
		target.setEnabled(source.isEnabled());
		target.setBusinessLatitude(source.getBusinessLatitude());
		target.setBusinessLongitude(source.getBusinessLongitude());
		target.setCreationDate(source.getCreationDate());

		if (source.getCategory() != null) {
			target.setCategoryName(source.getCategory().getName());
		}

		if (source.getUserModel() != null) {
			UserDTO userDTO = new UserDTO();
			userPopulator.populate(source.getUserModel(), userDTO);
			target.setUserDTO(userDTO);
		}

		if (source.getBusinessAddress() != null) {
			AddressDTO addressDTO = new AddressDTO();
			addressPopulator.populate(source.getBusinessAddress(), addressDTO);
			target.setAddressDTO(addressDTO);
		}

		if (source.getAttributes() != null && !source.getAttributes().isEmpty()) {
			Set<BusinessAttributeResponse> attributes = source.getAttributes().stream().map(attribute -> {
				BusinessAttributeResponse dto = new BusinessAttributeResponse();
				attributePopulator.populate(attribute, dto);
				return dto;
			}).collect(Collectors.toSet());
			target.setAttributes(attributes);
		}

	}
}
