package com.HostelManagement.HM.exception;

public class InvalidRoomState extends RuntimeException{
    private String msg;

    public InvalidRoomState(String msg){
        super(msg);
    }
}
