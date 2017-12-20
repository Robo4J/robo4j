package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    JsonException(String message) {
        super(message);
    }

    JsonException(String message, Throwable cause) {
        super(message, cause);
    }
}
