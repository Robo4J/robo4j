package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonBooleanAdapter implements JsonTypeAdapter<Boolean> {

    @Override
    public String adapt(Boolean obj) {
        return obj.toString();
    }
}
