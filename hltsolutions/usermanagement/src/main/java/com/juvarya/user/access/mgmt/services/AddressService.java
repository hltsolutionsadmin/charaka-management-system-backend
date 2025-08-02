package com.juvarya.user.access.mgmt.services;
import com.juvarya.user.access.mgmt.dto.AddressDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AddressService {
    AddressDTO saveOrUpdateAddress(AddressDTO addressDTO);
    AddressDTO getAddressById(Long id);
    void deleteAddressById(Long id);
    Page<AddressDTO> getAllAddresses(Long userId, Pageable pageable);
    Page<AddressDTO> getAddressesByBusinessId(Long businessId, Pageable pageable);
    public AddressDTO getDefaultAddress(Long userId);
    public AddressDTO setDefaultAddress(Long userId, Long addressId);
}
