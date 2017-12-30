package com.robo4j.socket.http;

public class HttpException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
