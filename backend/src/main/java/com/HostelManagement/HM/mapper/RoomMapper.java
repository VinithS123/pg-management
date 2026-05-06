package com.HostelManagement.HM.mapper;

import com.HostelManagement.HM.dto.RoomDto;
import com.HostelManagement.HM.model.RoomEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomDto toRoomDto(RoomEntity roomEntity);
    RoomEntity toRoomEntity(RoomDto roomDto);

    List<RoomDto> toRoomDtoList(List<RoomEntity> roomList);
}
