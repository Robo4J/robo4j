package com.robo4j.units.rpi.camera;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ImageDTOBuilder {

    private Integer width;
    private Integer height;
    private String encoding;
    private byte[] content;
    private ImageDTOBuilder(){
    }

    public static ImageDTOBuilder Build(){
        return new ImageDTOBuilder();
    }

    public ImageDTOBuilder setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public ImageDTOBuilder setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public ImageDTOBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public ImageDTOBuilder setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public ImageDTO build(){
        return new ImageDTO(width, height, encoding, content);
    }
}
