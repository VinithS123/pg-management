package com.HostelManagement.HM.controller;

import com.HostelManagement.HM.config.SecurityUtils;
import com.HostelManagement.HM.dto.RoomDto;
import com.HostelManagement.HM.enums.RoomStatus;
import com.HostelManagement.HM.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    private final SecurityUtils securityUtils;

    @PostMapping("/rooms")
    public ResponseEntity<RoomDto> addRoom(@RequestBody RoomDto roomDto){
        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(roomService.addRoom(roomDto,userId));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDto>> getRoomByStatus(@RequestParam RoomStatus status){
        long userId = securityUtils.getUserId();
        return ResponseEntity.ok(roomService.getRoomListByStatus(status,userId));
    }

    
}
