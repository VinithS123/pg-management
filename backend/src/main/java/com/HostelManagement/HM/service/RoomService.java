package com.HostelManagement.HM.service;

import com.HostelManagement.HM.dto.RoomDto;
import com.HostelManagement.HM.enums.RoomStatus;
import com.HostelManagement.HM.mapper.RoomMapper;
import com.HostelManagement.HM.model.CustomerModel;
import com.HostelManagement.HM.model.RoomEntity;
import com.HostelManagement.HM.repository.CustomerRepo;
import com.HostelManagement.HM.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;
    private final CustomerRepo customerRepo;

    private final RoomMapper mapper;
    public RoomDto addRoom(RoomDto roomDto, long userId) {
        roomDto.setUserId(userId);
        RoomEntity room = repository.save(mapper.toRoomEntity(roomDto));
        return mapper.toRoomDto(room);
    }

    public List<RoomDto> getRoomListByStatus(RoomStatus status, long userId) {
           List<RoomEntity> roomList = repository.findAllByUserIdAndRoomStatus(userId,status);
           return mapper.toRoomDtoList(roomList);
    }

    public List<RoomDto> getRoomOverview(long userId) {
        List<RoomEntity> rooms = repository.findAllByUserId(userId);
        List<CustomerModel> customers = customerRepo.findByUserId(userId);
        Map<Integer, Long> roomOccupancy = new HashMap<>();

        for (CustomerModel customer : customers) {
            roomOccupancy.merge(customer.getRoomNo(), 1L, Long::sum);
        }

        return rooms.stream()
                .map(room -> {
                    long occupiedBeds = roomOccupancy.getOrDefault(room.getRoomNo(), 0L);
                    int availableBeds = Math.max(room.getNoOfBed() - (int) occupiedBeds, 0);
                    RoomDto dto = mapper.toRoomDto(room);
                    dto.setOccupiedBeds((int) occupiedBeds);
                    dto.setAvailableBeds(availableBeds);
                    dto.setRoomStatus(occupiedBeds >= room.getNoOfBed() ? RoomStatus.RESERVED : RoomStatus.UNRESERVED);
                    return dto;
                })
                .toList();
    }
}
