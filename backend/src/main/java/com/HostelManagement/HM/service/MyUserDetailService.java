package com.HostelManagement.HM.service;
import com.HostelManagement.HM.model.UserPrincipal;
import com.HostelManagement.HM.model.Users;
import com.HostelManagement.HM.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {

    final UsersRepo usersRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user =  usersRepo.findByUserName(username);

        if(user == null){
            throw new UsernameNotFoundException("UserName : "+username+" Not Found");
        }

        return new UserPrincipal(user);

    }

    public  UserDetails loadUserByUserId(long userId){
        Users user = usersRepo.findByUserId(userId);

        if(user == null){
            throw new UsernameNotFoundException("UserID : "+userId+" Not Found");
        }
        return new UserPrincipal(user);
    }

    public Users registerUser(Users user) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        user.setUserPassword(bCryptPasswordEncoder.encode(user.getUserPassword()));
        //Stored User
        return usersRepo.save(user);
    }
}
