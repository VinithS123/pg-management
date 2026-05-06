package com.HostelManagement.HM.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class RentedAccessories {

    private String accessoriesName;
    private float accessoriesPrice;

}
