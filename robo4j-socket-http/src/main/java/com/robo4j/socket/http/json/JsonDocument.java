package com.robo4j.socket.http.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * representation of the read String stream
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonDocument {

    private final Map<String, Object> map = new LinkedHashMap<>();
    private final Type type;

    public JsonDocument(Type type) {
        this.type = type;
    }

    public void add(String key, Object value){
        map.put(key, value);
    }

    @Override
    public String toString() {
        return "JsonDocument{" +
                "map=" + map +
                ", type=" + type +
                '}';
    }

    public enum Type {
        OBJECT,
        ARRAY
    }
}
