package com.example.crio.cred.exceptions;

public class CardExpiredException extends RuntimeException {
    public CardExpiredException(String msg){
        super(msg);
    }
    
}
