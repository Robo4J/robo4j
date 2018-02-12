package com.robo4j.socket.http.message;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDecoratedRequest<T> implements DatagramMessage<T> {
    private String host;
    private Integer port;
    private T message;

    public DatagramDecoratedRequest(T message) {
        this.message = message;
    }

    @Override
    public T getMessage() {
        return message;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "DatagramDecoratedRequest{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", message=" + message +
                '}';
    }
}
