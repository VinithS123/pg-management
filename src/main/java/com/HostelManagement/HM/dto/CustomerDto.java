package com.HostelManagement.HM.dto;

import com.HostelManagement.HM.enums.CurrentStatus;
import com.HostelManagement.HM.enums.FeeStatus;
import com.HostelManagement.HM.enums.FrequencyType;
import com.HostelManagement.HM.enums.Sharing;
import com.HostelManagement.HM.model.RentedAccessories;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
//@NoArgsConstructor
@Builder
public class CustomerDto {

    private int id;
    private String name;
    private String phoneNo;
    private LocalDate joinDate;
    private LocalDate leaveDate;
    private float advanceAmount;
    private FrequencyType frequencyType;
    private FeeStatus feeStatus;
    private Sharing sharing;
    private float rentFee;
    private float totalFee;
    private CurrentStatus status;
    private long userId;
    @ElementCollection
    private List<RentedAccessories> rentedAccessoriesList;
}
