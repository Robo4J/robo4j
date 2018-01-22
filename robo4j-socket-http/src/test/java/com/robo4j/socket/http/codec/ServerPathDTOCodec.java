package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.ServerPathDTO;
import com.robo4j.socket.http.units.HttpProducer;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class ServerPathDTOCodec extends AbstractMessageCodec<ServerPathDTO> {
    public ServerPathDTOCodec() {
        super(ServerPathDTO.class);
    }
}
