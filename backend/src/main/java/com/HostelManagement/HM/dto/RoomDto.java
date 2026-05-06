package com.HostelManagement.HM.dto;

import com.HostelManagement.HM.enums.RoomStatus;
import lombok.Data;

@Data
public class RoomDto {

    private int roomNo;
    private int noOfBed;
    private int occupiedBeds;
    private int availableBeds;
    private long userId;
    private RoomStatus roomStatus;

}
