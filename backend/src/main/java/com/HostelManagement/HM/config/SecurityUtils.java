package com.HostelManagement.HM.config;

import com.HostelManagement.HM.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if((authentication!=null) && (authentication.getPrincipal() instanceof UserPrincipal user)){
            return user.getUserId();
        }

        throw new RuntimeException( "user not authenticated ");
    }

}
