package com.example.crio.cred.exceptions;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String msg){
        super(msg);
    }
}
