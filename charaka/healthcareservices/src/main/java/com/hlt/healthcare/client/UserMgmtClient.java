package com.hlt.healthcare.client;


import com.hlt.commonservice.dto.*;
import com.hlt.commonservice.enums.ERole;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "usermanagement",
        url = "${juvarya.usermgmt.url}"
)
public interface UserMgmtClient {

    @GetMapping("api/usermanagement/by-contact/{contact}")
    UserDTO getUserByPrimaryContact(@Valid @RequestParam("primaryContact") String primaryContact);

    @GetMapping("/api/usermanagement/business/{id}")
    B2BUnitDTO getBusinessById(@PathVariable("id") Long id);

    @GetMapping("/api/usermanagement/role/find/{erole}")
    Role getByERole(@PathVariable("erole") ERole eRole);

    @PostMapping("/api/usermanagement/user/save")
    UserDTO saveUser(@RequestBody UserDTO user);

    @GetMapping("/api/usermanagement/user/find/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    @PostMapping("/api/usermanagement/user/details/all")
    List<UserDTO> getUserDetailsByIds(@RequestBody List<Long> userIds);

    @PutMapping("/api/usermanagement/user/user/{userId}/role/{role}")
    void addRole(@PathVariable("userId") Long userId, @PathVariable("role") ERole eRole);

    @GetMapping("/api/usermanagement/user/contact")
    LoggedInUser getByPrimaryContact(@Valid @RequestParam("primaryContact") String primaryContact);

    @PostMapping("/api/usermanagement/user/onboard/user")
    Long onBoardUser(@Valid @RequestBody BasicOnboardUserDTO basicOnboardUserDTO);

    @DeleteMapping("/api/usermanagement/user/contact/{mobileNumber}/role/{role}")
    void removeUserRole(@PathVariable("mobileNumber") String mobileNumber, @PathVariable("role") ERole userRole);

}
