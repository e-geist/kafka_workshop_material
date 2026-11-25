package com.example.springconsumer;

// Exception für temporäres Problem - bspw. Datenbankverbindung -> Wiederholung sinnvoll
public class TransientException extends RuntimeException {
    public TransientException(String msg) {
        super(msg);
    }
}
