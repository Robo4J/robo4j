package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonBooleanAdapter extends JsonAdapter<Boolean> {

    @Override
    public String internalAdapt(Boolean obj) {
        return obj.toString();
    }
}
