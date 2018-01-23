package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;

/**
 * Response Decoder Unit DTO
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ResponseDecoderUnitDTO {
    private String id;
    private String codec;
    private List<HttpMethod> methods;


    public ResponseDecoderUnitDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<HttpMethod> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "ResponseDecoderUnitDTO{" +
                "id='" + id + '\'' +
                ", codec='" + codec + '\'' +
                ", methods=" + methods +
                '}';
    }
}
