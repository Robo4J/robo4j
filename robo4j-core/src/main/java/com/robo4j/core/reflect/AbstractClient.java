/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This AbstractClient.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.reflect;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import com.robo4j.commons.annotation.RoboMotor;
import com.robo4j.commons.annotation.RoboProvider;
import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.commons.annotation.RoboService;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.annotation.RoboUnitProducer;
import com.robo4j.core.client.ClientHTTPExecutor;

/**
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 09.06.2016
 */
public abstract class AbstractClient<FutureType> {

	private static ExecutorService executor;
	protected AtomicBoolean active;

	protected AbstractClient(RoboReflectionScan scan) {
		active = new AtomicBoolean(false);
		executor = new ClientHTTPExecutor();
		//@format:off
		//TODO can be simplified
		final List<Class<? extends Annotation>> usedAnnotation = Collections.unmodifiableList(new LinkedList<Class<? extends Annotation>>(){{
			add(RoboMotor.class);
			add(RoboSensor.class);
			add(RoboUnitProducer.class);
			add(RoboUnit.class);
			add(RoboService.class);
			add(RoboProvider.class);
		}});
		final Map<Class<? extends Annotation>, Stream<Class<?>>> coreMap = Collections
				.unmodifiableMap(new HashMap<Class<? extends Annotation>, Stream<Class<?>>>(){{
					usedAnnotation.forEach(
							anno -> put(anno, scan.getClassesByAnnotation(anno)));
				}});
		//@format:on
		new RoboReflectiveInit(coreMap);
		active.set(true);
	}

	//TODO: probably remove
	public Future<FutureType> submit(Callable<FutureType> task) {
		return executor.submit(task);
	}

	public boolean isActive(){
		return active.get();
	}

	public void end() {
		executor.shutdown();
	}

}
