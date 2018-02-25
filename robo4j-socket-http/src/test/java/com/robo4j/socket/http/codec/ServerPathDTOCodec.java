package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.units.HttpProducer;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class ServerPathDTOCodec extends AbstractHttpMessageCodec<ServerUnitPathDTO> {
    public ServerPathDTOCodec() {
        super(ServerUnitPathDTO.class);
    }
}
