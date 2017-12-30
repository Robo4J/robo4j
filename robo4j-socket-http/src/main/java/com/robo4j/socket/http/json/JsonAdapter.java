package com.robo4j.socket.http.json;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class JsonAdapter<T> implements JsonTypeAdapter {

    @SuppressWarnings("unchecked")
    @Override
    public String adapt(Object obj) {
        return internalAdapt((T)obj);
    }

    protected abstract String internalAdapt(T obj);
}
