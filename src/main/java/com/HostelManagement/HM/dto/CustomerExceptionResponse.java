package com.HostelManagement.HM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CustomerExceptionResponse {

    private LocalDateTime timeStamp;
    private String message;
    private String details;
    private String path;
}
