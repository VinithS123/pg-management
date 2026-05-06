package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Users> registerUser(@RequestBody Users users){
        return  ResponseEntity.ok(userService.registerUser(users));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody Users users){

        return ResponseEntity.ok(userService.verifyUser(users));
    }
}
