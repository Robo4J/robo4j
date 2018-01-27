package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.units.ClientPathConfig;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientClassPathDTO {

    private final String clazz;
    private final ClientPathConfig clientPath;

    public ClientClassPathDTO(String clazz, ClientPathConfig clientPath) {
        this.clazz = clazz;
        this.clientPath = clientPath;
    }

    public String getClazz() {
        return clazz;
    }

    public ClientPathConfig getClientPath() {
        return clientPath;
    }
}
