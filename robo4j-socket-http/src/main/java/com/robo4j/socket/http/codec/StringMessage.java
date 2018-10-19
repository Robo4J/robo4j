package com.robo4j.socket.http.codec;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class StringMessage implements Serializable {

    private String message;

    public StringMessage() {
    }

    public StringMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMessage that = (StringMessage) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "StringMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
