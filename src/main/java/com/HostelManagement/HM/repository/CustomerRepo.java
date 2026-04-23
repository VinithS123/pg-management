package com.HostelManagement.HM.repository;

import com.HostelManagement.HM.model.CustomerModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
    public interface CustomerRepo extends JpaRepository<CustomerModel,Integer> {

    boolean existsByName(String name);
//    List<CustomerModel> findByJoinDate(LocalDate date);

//    CustomerModel findALLByJoinDateContaining();

    List<CustomerModel> findALLByJoinDate(LocalDate localDate);

    List<CustomerModel> findAllByName(String name);


    @Query("Select u from CustomerModel u where u.name like %:name%")
    List<CustomerModel>findByNameHaving(String name);

    Optional<CustomerModel> findByIdAndUserId(int id, long userId);

    List<CustomerModel> findByUserId(long userId, Pageable pageable);

    List<CustomerModel> findByUserId(long userId);


    @Query(""" 
            SELECT c FROM CustomerModel c
            WHERE c.userId = :userId
            AND( LOWER(c.name) LIKE LOWER(CONCAT('%',:keyword,'%')) 
                 OR LOWER(c.phoneNo) LIKE LOWER(CONCAT('%',:keyword,'%')) )""")
    List<CustomerModel> findProductByKeyword(String keyword, long userId, Pageable pageable);
}
