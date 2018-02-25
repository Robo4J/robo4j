package com.robo4j.socket.http.units;

/**
 * ClientMessageWrapper wraps information for appropriate codec client
 *
 * @see DatagramClientCodecUnit
 * @see HttpClientCodecUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientMessageWrapper {

    private String path;
    private Class<?> clazz;
    private Object message;

    public ClientMessageWrapper(String path, Class<?> clazz, Object message) {
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
        return "ClientMessageWrapper{" +
                "path='" + path + '\'' +
                ", clazz=" + clazz +
                ", message=" + message +
                '}';
    }
}
