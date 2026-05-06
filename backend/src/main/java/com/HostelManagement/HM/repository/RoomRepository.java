package com.HostelManagement.HM.repository;

import com.HostelManagement.HM.enums.RoomStatus;
import com.HostelManagement.HM.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity,Integer> {

    List<RoomEntity> findAllByUserId(long userId);
    Optional<RoomEntity> findByUserIdAndRoomNo(long id, int roomNo);
    List<RoomEntity> findAllByUserIdAndRoomStatus(long userId, RoomStatus roomStatus);
}
