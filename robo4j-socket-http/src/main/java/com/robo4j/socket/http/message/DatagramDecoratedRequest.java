package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.ChannelBufferUtils;

import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDecoratedRequest implements DatagramMessage<byte[]> {

    private final DatagramDenominator denominator;
    private String host;
    private Integer port;
    private byte[] message;

    public DatagramDecoratedRequest(DatagramDenominator denominator) {
        this.denominator = denominator;
    }

    @Override
    public byte[] getMessage() {
        return ChannelBufferUtils.joinByteArrays(denominator.generate(), message);
    }

    @Override
    public void addMessage(byte[] message) {
        this.message = this.message == null ? message : ChannelBufferUtils.joinByteArrays(this.message, message);
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
                ", message=" + Arrays.asList(message) +
                '}';
    }
}
