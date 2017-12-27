package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonNumberAdapter implements JsonTypeAdapter<Integer>{

    @Override
    public String adapt(Integer obj) {
		return String.valueOf(obj);
    }
}
