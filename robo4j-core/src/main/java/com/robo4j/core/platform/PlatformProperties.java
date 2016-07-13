/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This PlatformProperties.java is part of robo4j.
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

package com.robo4j.core.platform;

import com.robo4j.core.platform.command.LegoCommandProperty;

/**
 *
 * Holder for platform properties
 *
 * 200(22)
 * 100(11)
 * currently only basic calculation
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 31.03.2016
 */
public class PlatformProperties {

    private static final int DEFAULT_CYCLES = 300;
    private static final int DEFAULT_CENTIMETER_CYCLES = 33;
    private static final int DEFAULT_ROTATION_CYCLES = 9;

    /* default engine speed  */
    private int cycles;
    /* cycles calculation  */
    private int centimeterCycles;
    private int angleCycles;

    public PlatformProperties() {
        this.cycles = DEFAULT_CYCLES;
        this.centimeterCycles = DEFAULT_CENTIMETER_CYCLES;
        this.angleCycles = DEFAULT_ROTATION_CYCLES;
    }

    public PlatformProperties(int cycles, int centimeterCycles) {
        this.cycles = cycles;
        this.centimeterCycles = centimeterCycles;
        this.angleCycles = DEFAULT_ROTATION_CYCLES;
    }

    public int getCycles() {
        return cycles;
    }

    public void setCycles(String cycles){
        this.cycles = Integer.valueOf(cycles.trim());
    }

    public void setCycles(int cycles) {
        this.cycles = cycles;
    }

    public int getCentimeterCycles() {
        return centimeterCycles;
    }

    public void setCentimeterCycles(int centimeterCycles) {
        this.centimeterCycles = centimeterCycles;
    }

    public int getAngleCycles() {
        return angleCycles;
    }

    public void setAngleCycles(int angleCycles) {
        this.angleCycles = angleCycles;
    }

    public LegoCommandProperty getCycleCommandProperty(){
        return new LegoCommandProperty(String.valueOf(cycles));
    }

    public void reset(){
        this.cycles = DEFAULT_CYCLES;
        this.centimeterCycles = DEFAULT_CENTIMETER_CYCLES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlatformProperties)) return false;

        PlatformProperties that = (PlatformProperties) o;

        if (cycles != that.cycles) return false;
        return centimeterCycles == that.centimeterCycles;

    }

    @Override
    public int hashCode() {
        int result = cycles;
        result = 31 * result + centimeterCycles;
        return result;
    }

    @Override
    public String toString() {
        return "PlatformProperties{" +
                "cycles=" + cycles +
                ", centimeterCycles=" + centimeterCycles +
                '}';
    }
}
