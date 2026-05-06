package com.HostelManagement.HM.model;

import com.HostelManagement.HM.enums.CurrentStatus;
import com.HostelManagement.HM.enums.FeeStatus;
import com.HostelManagement.HM.enums.FrequencyType;
import com.HostelManagement.HM.enums.Sharing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CustomerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String phoneNo;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private Integer roomNo;
    private float advanceAmount;

    @Enumerated(EnumType.STRING)
    private FrequencyType frequencyType;

    @Enumerated(EnumType.STRING)
    private FeeStatus feeStatus;

    @Enumerated(EnumType.STRING)
    private Sharing sharing;

    private float rentFee;
    private float totalFee;

    @Enumerated(EnumType.STRING)
    private CurrentStatus status;
    private long userId;

    //Custom accessories rent
    @ElementCollection
    private List<RentedAccessories> rentedAccessoriesList;
}


