package com.example.crio.cred.exceptions;

public class EmailAleadyExistsException extends RuntimeException {

    public EmailAleadyExistsException(String msg){
        super(msg);
    }
    
}
