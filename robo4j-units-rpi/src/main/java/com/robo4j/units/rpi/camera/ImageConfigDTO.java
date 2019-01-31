/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
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
