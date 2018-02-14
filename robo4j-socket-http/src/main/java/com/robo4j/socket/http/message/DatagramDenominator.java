package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.RoboHttpUtils;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDenominator  implements MessageDenominator<byte[]>{
    private final StringBuilder sb = new StringBuilder();
    private final String path;

    public DatagramDenominator(String path) {
        this.path = path;
    }

    @Override
    public byte[] generate() {
        sb.append(path);
        RoboHttpUtils.decorateByNewLine(sb);
        return sb.toString().getBytes();
    }
}
