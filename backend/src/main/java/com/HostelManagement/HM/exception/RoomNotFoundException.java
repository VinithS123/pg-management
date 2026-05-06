package com.HostelManagement.HM.exception;

public class RoomNotFoundException extends RuntimeException{

    String msg;

    public RoomNotFoundException(String msg){
        super(msg);
    }
}
