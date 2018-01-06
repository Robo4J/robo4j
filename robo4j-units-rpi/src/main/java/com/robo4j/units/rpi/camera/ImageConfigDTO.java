package com.robo4j.units.rpi.camera;

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
    private final int timeout;
    private final int timelapse;

    public ImageConfigDTO(int height, int width, int brightness, int timeout, int timelapse) {
        this.height = height;
        this.width = width;
        this.brightness = brightness;
        this.timeout = timeout;
        this.timelapse = timelapse;
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

    public int getTimeout() {
        return timeout;
    }

    public int getTimelapse() {
        return timelapse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageConfigDTO that = (ImageConfigDTO) o;
        return height == that.height &&
                width == that.width &&
                brightness == that.brightness &&
                timeout == that.timeout &&
                timelapse == that.timelapse;
    }

    @Override
    public int hashCode() {

        return Objects.hash(height, width, brightness, timeout, timelapse);
    }

    @Override
    public String toString() {
        return "ImageConfigDTO{" +
                "height=" + height +
                ", width=" + width +
                ", brightness=" + brightness +
                ", timeout=" + timeout +
                ", timelapse=" + timelapse +
                '}';
    }
}
