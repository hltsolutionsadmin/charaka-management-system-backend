package com.hlt.usermanagement.controllers;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;


    @GetMapping("/find/{erole}")
    public RoleModel getByERole(@PathVariable("erole") ERole eRole) {
        return roleService.findByErole(eRole);
    }

}
