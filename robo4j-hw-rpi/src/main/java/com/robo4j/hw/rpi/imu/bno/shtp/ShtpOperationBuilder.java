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

package com.robo4j.hw.rpi.imu.bno.shtp;

/**
 * ShtpOperationBuilder provides a chain of operation needs to by processed in desired order
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpOperationBuilder {

    private final ShtpOperation head;
    private ShtpOperation current;

    public ShtpOperationBuilder(ShtpOperation head) {
        this.head = head;
        this.current = head;
    }

    public ShtpOperationBuilder addOperation(ShtpOperation operation){
        current.setNext(operation);
        current = operation;
        return this;
    }

    public ShtpOperation build(){
        return head;
    }
}
