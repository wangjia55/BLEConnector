package com.jacob.ble.connector.exception;

public class InitException extends Exception {

    public InitException(String message) {
        super("Google ble init: " + message);
    }
}
