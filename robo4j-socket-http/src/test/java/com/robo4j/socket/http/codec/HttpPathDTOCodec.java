package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.units.HttpProducer;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class HttpPathDTOCodec extends AbstractHttpMessageCodec<HttpPathMethodDTO> {
    public HttpPathDTOCodec() {
        super(HttpPathMethodDTO.class);
    }
}
