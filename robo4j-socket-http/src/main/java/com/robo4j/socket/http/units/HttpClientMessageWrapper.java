package com.robo4j.socket.http.units;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpClientMessageWrapper {

    private String path;
    private Class<?> clazz;
    private Object message;

    public HttpClientMessageWrapper(String path, Class<?> clazz, Object message) {
        this.path = path;
        this.clazz = clazz;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "HttpClientMessageWrapper{" +
                "path='" + path + '\'' +
                ", clazz=" + clazz +
                ", message=" + message +
                '}';
    }
}
