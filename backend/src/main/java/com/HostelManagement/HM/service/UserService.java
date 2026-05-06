package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    final MyUserDetailService myUserDetailService;
    final AuthenticationManager authenticationManager;
    final JwtService jwtService;
    final UsersRepo usersRepo;

    public Users registerUser(Users users) {

        return myUserDetailService.registerUser(users);
    }

    public String verifyUser(Users users) {

        //Here the authenticate() calls the DaoAuthentication internally
        //which calls the loadUserByUserName()  internally which
        // checks in the database(Overridden by us)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(users.getUserName(),
                        users.getUserPassword()));

        if(authentication.isAuthenticated()){
            long userId = findIdByUsername(users);
            users.setUserId(userId);
            return jwtService.generateToken(users);
        }

        return "User Verification Unsuccessful";
    }

    public long findIdByUsername(Users users) {
        return usersRepo.findByUserName(users.getUserName()).getUserId();
    }
}
