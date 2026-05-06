package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.repository.UsersRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyUserDetailServiceTest {

    @Mock
    UsersRepo usersRepo;

    @InjectMocks
    MyUserDetailService myUserDetailService;

    @Test
    void loadUserByUsernameSuccess() {
        Users user = new Users();
        user.setUserName("ramesh");
        user.setUserPassword("password");

        when(usersRepo.findByUserName("ramesh")).thenReturn(user);

        UserDetails result = myUserDetailService.loadUserByUsername("ramesh");

        assertEquals("ramesh",result.getUsername());
        assertEquals("password",result.getPassword());
        verify(usersRepo,times(1)).findByUserName("ramesh");
    }

    @Test
    void loadUserByUserIdSuccess() {
        Users user = new Users();
        user.setUserId(100L);
        user.setUserName("suresh");
        user.setUserPassword("password");

        when(usersRepo.findByUserId(100L)).thenReturn(user);

        UserDetails result = myUserDetailService.loadUserByUserId(100L);

        assertEquals("suresh",result.getUsername());
        assertEquals("password",result.getPassword());
        verify(usersRepo,times(1)).findByUserId(100L);
    }

    @Test
    void registerUserSuccess() {
        Users user = new Users();
        user.setUserName("ramesh");
        user.setUserPassword("plainPass");

        when(usersRepo.save(any(Users.class))).thenReturn(user);

        Users result = myUserDetailService.registerUser(user);

        assertEquals("ramesh",result.getUserName());
        assertNotEquals("plainPass",result.getUserPassword());
        assertTrue(new BCryptPasswordEncoder(12).matches("plainPass",result.getUserPassword()));
        verify(usersRepo).save(any(Users.class));
    }

    @Test
    void loadUserByUsernameFailure() {
        when(usersRepo.findByUserName("ramesh")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                myUserDetailService.loadUserByUsername("ramesh"));

        assertEquals("UserName : ramesh Not Found", exception.getMessage());
        verify(usersRepo, times(1)).findByUserName("ramesh");
    }

    @Test
    void loadUserByUserIdFailure() {
        when(usersRepo.findByUserId(100L)).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                myUserDetailService.loadUserByUserId(100L));

        assertEquals("UserID : 100 Not Found", exception.getMessage());
        verify(usersRepo, times(1)).findByUserId(100L);
    }
}
