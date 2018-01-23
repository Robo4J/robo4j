package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.units.ServerPathConfig;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ContextPathDTO {
    private final String unitId;
    private final ServerPathConfig serverPathConfig;

    public ContextPathDTO(String unitId, ServerPathConfig serverPathConfig) {
        this.unitId = unitId;
        this.serverPathConfig = serverPathConfig;
    }

    public String getUnitId() {
        return unitId;
    }

    public ServerPathConfig getServerPathConfig() {
        return serverPathConfig;
    }

    @Override
    public String toString() {
        return "ContextPathDTO{" +
                "unitId='" + unitId + '\'' +
                ", serverPathConfig=" + serverPathConfig +
                '}';
    }
}
