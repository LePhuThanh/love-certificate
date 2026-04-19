package com.phelim.system.love_certificate.exception;

public class SimulatedTimeoutException extends RuntimeException {

    public SimulatedTimeoutException(String message) {
        super(message);
    }

    public SimulatedTimeoutException() {
        super("SIMULATED_TIMEOUT");
    }
}