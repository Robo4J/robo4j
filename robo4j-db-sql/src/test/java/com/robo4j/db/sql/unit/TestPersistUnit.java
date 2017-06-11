/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.db.sql.unit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.db.sql.jpa.RoboPersitenceUnit;
import com.robo4j.db.sql.model.ERoboPoint;
import com.robo4j.db.sql.model.ERoboUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TestPersistUnit extends RoboPersitenceUnit<TestPersistPointDTO> {


    public TestPersistUnit(RoboContext context, String id) {
        super(TestPersistPointDTO.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {

    }

    @Override
    public void onMessage(TestPersistPointDTO message) {
        ERoboUnit roboUnit = getEntity();
        ERoboPoint point = new ERoboPoint();
        point.setUnit(roboUnit);
        point.setValueType(message.getValueType());
        point.setValues(message.getValue());
        roboUnit.addPoint(point);
        save(roboUnit);
    }
}
