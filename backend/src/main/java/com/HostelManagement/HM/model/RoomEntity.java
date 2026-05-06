package com.HostelManagement.HM.model;

import com.HostelManagement.HM.enums.RoomStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RoomEntity {

    @Id
    private int roomNo;
    private int noOfBed;
    private int occupiedBeds;
    private long userId;
    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

}
