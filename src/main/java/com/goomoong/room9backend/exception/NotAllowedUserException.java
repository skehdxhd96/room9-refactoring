package com.goomoong.room9backend.exception;

public class NotAllowedUserException extends RuntimeException{
    public NotAllowedUserException(String message){
        super(message);
    }
}
