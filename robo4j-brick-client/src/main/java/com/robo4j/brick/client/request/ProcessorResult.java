/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ProcessorResult.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.brick.client.request;

/**
 * Result of HTTP Request processor
 * Created by miroslavkopecky on 18/06/16.
 */
public final class ProcessorResult {

    private String status;
    private String message;

    public ProcessorResult(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ProcessorResult{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
