package com.example.crio.cred.exceptions;

public class LoginConflictException extends RuntimeException{
    public LoginConflictException(String msg){
        super(msg);
    }
}
