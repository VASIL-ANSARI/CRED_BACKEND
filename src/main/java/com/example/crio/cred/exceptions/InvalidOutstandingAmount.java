package com.example.crio.cred.exceptions;

public class InvalidOutstandingAmount extends RuntimeException {
    public InvalidOutstandingAmount(String msg){
        super(msg);
    }
}
