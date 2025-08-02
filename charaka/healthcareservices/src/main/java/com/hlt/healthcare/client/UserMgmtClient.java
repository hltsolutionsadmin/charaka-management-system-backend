package com.hlt.delivery.client;

import com.hlt.commonservice.dto.*;
import com.hlt.commonservice.enums.ERole;
import com.hlt.delivery.dto.AddressDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "userMgmtService")
public interface UserMgmtClient {

    @GetMapping("/api/usermgmt/api/addresses/{addressId}")
    AddressDTO getAddressById(@PathVariable("addressId") Long id);

    @GetMapping("/api/usermgmt/user/count/business/{businessId}")
    ResponseEntity<StandardResponse<Long>> getUserCountByBusinessId(@PathVariable("businessId") Long businessId);

    @GetMapping("/api/usermgmt/business/{id}")
    B2BUnitDTO getBusinessById(@PathVariable("id") Long id);

    @GetMapping("/api/usermgmt/role/find/{erole}")
    Role getByERole(@PathVariable("erole") ERole eRole);

    @PostMapping("/api/usermgmt/user/save")
    UserDTO saveUser(@RequestBody UserDTO user);

    @GetMapping("/api/usermgmt/user/find/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/api/usermgmt/user/details/all")
    List<UserDTO> getUserDetailsByIds(@RequestBody List<Long> userIds);

    @PutMapping("/api/usermgmt/user/user/{userId}/role/{role}")
    void addRole(@PathVariable("userId") Long userId, @PathVariable("role") ERole eRole);

    @GetMapping("/api/usermgmt/user/contact")
    LoggedInUser getByPrimaryContact(@Valid @RequestParam("primaryContact") String primaryContact);

    @PostMapping("/api/usermgmt/user/onboard/user")
    Long onBoardUser(@Valid @RequestBody BasicOnboardUserDTO basicOnboardUserDTO);

    @DeleteMapping("/api/usermgmt/user/contact/{mobileNumber}/role/{role}")
    void removeUserRole(@PathVariable("mobileNumber") String mobileNumber, @PathVariable("role") ERole userRole);



}

