package com.example.crio.cred.exceptions;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String msg){
        super(msg);
    }
}
