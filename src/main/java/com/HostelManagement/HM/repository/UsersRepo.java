package com.HostelManagement.HM.repository;

import com.HostelManagement.HM.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<Users,Integer> {

    public Users findByUserName(String userName);

    public Users findByUserId(long userId);

}
