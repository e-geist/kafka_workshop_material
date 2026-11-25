package com.example.springconsumer;

// Exception für fehlerhafte Nachricht - bspw. fachlich ungültig -> Wiederholung sinnlos
public class PoisonPillException extends RuntimeException {
    public PoisonPillException(String msg) {
        super(msg);
    }
}
