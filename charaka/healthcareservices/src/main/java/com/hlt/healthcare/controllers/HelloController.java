package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping
    public ResponseEntity<String> sayHi() {
        return ResponseEntity.ok("Hi");
    }
//

}
