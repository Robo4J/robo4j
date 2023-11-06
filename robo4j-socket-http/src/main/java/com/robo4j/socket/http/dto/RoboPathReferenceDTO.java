/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.dto;

import com.robo4j.RoboReference;
import com.robo4j.socket.http.enums.SystemPath;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboPathReferenceDTO {

    private final SystemPath path;
    private final RoboReference<?> roboReference;

    public RoboPathReferenceDTO(SystemPath path, RoboReference<?> roboReference) {
        this.path = path;
        this.roboReference = roboReference;
    }

    public SystemPath getPath() {
        return path;
    }

    public RoboReference<?> getRoboReference() {
        return roboReference;
    }

    @Override
    public String toString() {
        return "RoboPathReferenceDTO{" +
                "path=" + path +
                ", roboReference=" + roboReference +
                '}';
    }
}
