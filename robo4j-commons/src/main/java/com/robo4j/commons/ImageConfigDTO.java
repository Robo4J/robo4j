package com.robo4j.commons;

import java.util.Objects;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ImageConfigDTO {
    private final int height;
    private final int width;
    private final int brightness;

    public ImageConfigDTO(int height, int width, int brightness) {
        this.height = height;
        this.width = width;
        this.brightness = brightness;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getBrightness() {
        return brightness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageConfigDTO that = (ImageConfigDTO) o;
        return height == that.height &&
                width == that.width &&
                brightness == that.brightness;
    }

    @Override
    public int hashCode() {

        return Objects.hash(height, width, brightness);
    }

    @Override
    public String toString() {
        return "ImageConfigDTO{" +
                "height=" + height +
                ", width=" + width +
                ", brightness=" + brightness +
                '}';
    }
}
