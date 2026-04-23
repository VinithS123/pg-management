package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    Users user;

    @BeforeEach
    void setup(){
        user = new Users();
    }



    @Test
    void registerUserSuccess() {

        when(userService.registerUser(any(Users.class))).thenReturn(user);

        ResponseEntity<Users> response = userController.registerUser(user);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(user,response.getBody());

    }

    @Test
    void loginUserSuccess() {
        String jwt = "bearer_token";
        when(userService.verifyUser(any(Users.class))).thenReturn(jwt);

        ResponseEntity<String> response = userController.loginUser(user);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(jwt,response.getBody());

        verify(userService,times(1)).verifyUser(any(Users.class));
    }
}
