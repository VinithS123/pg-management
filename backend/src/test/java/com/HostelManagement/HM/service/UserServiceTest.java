package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.repository.UsersRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    MyUserDetailService myUserDetailService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtService jwtService;

    @Mock
    UsersRepo usersRepo;

    @Mock
    Authentication authentication;

    @InjectMocks
    UserService userService;

    @Test
    void registerUserSuccess() {
        Users user = new Users();
        when(myUserDetailService.registerUser(user)).thenReturn(user);

        Users result = userService.registerUser(user);

        assertEquals(user, result);
        verify(myUserDetailService, times(1)).registerUser(user);
    }

    @Test
    void verifyUserSuccess() {
        Users user = new Users();
        user.setUserName("ramesh");
        user.setUserPassword("pass");

        Users foundUser = new Users();
        foundUser.setUserId(100L);
        foundUser.setUserName("ramesh");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(usersRepo.findByUserName("ramesh")).thenReturn(foundUser);
        when(jwtService.generateToken(user)).thenReturn("token_123");

        String result = userService.verifyUser(user);

        assertEquals("token_123", result);
        assertEquals(100L, user.getUserId());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void verifyUserFailure() {
        Users user = new Users();
        user.setUserName("ramesh");
        user.setUserPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String result = userService.verifyUser(user);

        assertEquals("User Verification Unsuccessful", result);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, never()).generateToken(any(Users.class));
    }

    @Test
    void findIdByUsernameSuccess() {
        Users inputUser = new Users();
        inputUser.setUserName("ramesh");

        Users foundUser = new Users();
        foundUser.setUserId(100L);
        foundUser.setUserName("ramesh");

        when(usersRepo.findByUserName("ramesh")).thenReturn(foundUser);

        long result = userService.findIdByUsername(inputUser);

        assertEquals(100L, result);
        verify(usersRepo, times(1)).findByUserName("ramesh");
    }

    @Test
    void findIdByUsernameFailure() {
        Users inputUser = new Users();
        inputUser.setUserName("ramesh");
        when(usersRepo.findByUserName("ramesh")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> userService.findIdByUsername(inputUser));
        verify(usersRepo, times(1)).findByUserName("ramesh");
    }
}
