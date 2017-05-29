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

package com.robo4j.db.sql;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SQLDbUnit extends RoboUnit<Object> {

    private static final String PERSISTENCE_UNIT = "persistenceUnit";
	private String persistanceUnit;
	private EntityManager em;

	public SQLDbUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		persistanceUnit = configuration.getString(PERSISTENCE_UNIT, null);
		if (persistanceUnit == null) {
            throw ConfigurationException.createMissingConfigNameException(PERSISTENCE_UNIT);
		}
	}

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        em = Persistence.createEntityManagerFactory(persistanceUnit).createEntityManager();
        setState(LifecycleState.STARTED);
    }
}
