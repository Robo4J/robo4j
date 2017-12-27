package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonNumberAdapter extends JsonAdapter<Integer>{

    public JsonNumberAdapter() {
    }

    @Override
    protected String internalAdapt(Integer obj) {
        return String.valueOf(obj);
    }
}
