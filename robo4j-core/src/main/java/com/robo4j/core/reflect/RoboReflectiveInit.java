/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This RoboReflectiveInit.java  is part of robo4j.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.robo4j.commons.annotation.RoboMotor;
import com.robo4j.commons.annotation.RoboProvider;
import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.commons.annotation.RoboService;
import com.robo4j.commons.annotation.RoboUnit;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.registry.BaseRegistryProvider;
import com.robo4j.commons.registry.RegistryManager;
import com.robo4j.commons.sensor.GenericSensor;
import com.robo4j.commons.service.GenericService;
import com.robo4j.commons.unit.GenericUnit;
import com.robo4j.core.client.io.ClientException;

/**
 * Initial implementation of robo-client reflection usage
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 16.06.2016
 */

class RoboReflectiveInit {

	@SuppressWarnings(value = "unchecked")
	RoboReflectiveInit(Stream<Class<?>> engines, Stream<Class<?>> sensors, Stream<Class<?>> units,
			Stream<Class<?>> services, Stream<Class<?>> providers) {

		/* initiation of all caches used at the run time */
		ExecutorService executors = Executors.newFixedThreadPool(RegistryUtil.registry.size());
		Future<Map<String, GenericMotor>> futureEngineCache = executors.submit(() -> initEngineCache(engines));
		Future<Map<String, GenericSensor>> futureSensorsCache = executors.submit(() -> initSensorCache(sensors));
		Future<Map<String, GenericUnit>> futureUnitsCache = executors.submit(() -> initUnitCache(units));
		Future<Map<String, GenericService>> futureServicesCache = executors.submit(() -> initServiceCache(services));
		Future<Map<String, BaseRegistryProvider>> futureProviderCache = executors
				.submit(() -> initProviderCache(providers));

		try {
			final Map<String, GenericMotor> initEngines = futureEngineCache.get();
			final Map<String, GenericSensor> initSensors = futureSensorsCache.get();
			final Map<String, GenericUnit> initUnits = futureUnitsCache.get();
			final Map<String, GenericService> initServices = futureServicesCache.get();
			final Map<String, BaseRegistryProvider> initProviders = futureProviderCache.get();
			RegistryManager registryManager = RegistryManager.getInstance().addAll(RegistryUtil.registry);
			if (initEngines != null && initSensors != null && initUnits != null && initServices != null
					&& initProviders != null) {

				if (registryManager.isActive()) {
					registryManager.initRegistry(RegistryTypeEnum.PROVIDER, futureProviderCache.get());
					registryManager.initRegistry(RegistryTypeEnum.ENGINES, futureEngineCache.get());
					registryManager.initRegistry(RegistryTypeEnum.SENSORS, futureSensorsCache.get());
					registryManager.initRegistry(RegistryTypeEnum.UNITS, futureUnitsCache.get());
					registryManager.initRegistry(RegistryTypeEnum.SERVICES, futureServicesCache.get());
				} else {
					SimpleLoggingUtil.debug(getClass(), "InitRegistry failed");
				}

			}
		} catch (InterruptedException | ExecutionException e) {
			throw new ClientException("CACHE GENERAL INIT ISSUE ", e);
		} finally {
			executors.shutdown();
			SimpleLoggingUtil.debug(getClass(), "executors down");
		}

	}

	// Private Methods
	@SuppressWarnings(value = "unchecked")
	private <EngineType extends GenericMotor> Map<String, EngineType> initEngineCache(Stream<Class<?>> engines) {
		try {
			final Map<String, EngineType> result = new HashMap<>();

			for (Iterator<?> iterator = engines.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboMotor.class)) {
					RoboMotor anno = clazz.getAnnotation(RoboMotor.class);
					SimpleLoggingUtil.debug(getClass(), "ref->engine= " + anno.id());
					result.put(anno.id(), (EngineType) clazz.newInstance());
				}
			}
			return result;
		} catch (Exception e) {
			throw new ClientException("ENGINE CACHE PROBLEM", e);
		}

	}

	@SuppressWarnings(value = "unchecked")
	private <SensorType extends GenericSensor> Map<String, SensorType> initSensorCache(Stream<Class<?>> sensors) {
		try {
			final Map<String, SensorType> result = new HashMap<>();
			for (Iterator<?> iterator = sensors.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboSensor.class)) {
					RoboSensor anno = clazz.getAnnotation(RoboSensor.class);
					SimpleLoggingUtil.debug(getClass(), "ref->sensor= " + anno.id());
					result.put(anno.id(), (SensorType) clazz.newInstance());
				}
			}
			return result;
		} catch (Exception e) {
			throw new ClientException("SENSOR CACHE PROBLEM", e);
		}

	}

	// TODO: FIMXE -> create logic to check hardware resources
	@SuppressWarnings(value = "unchecked")
	private <UnitType extends GenericUnit> Map<String, UnitType> initUnitCache(Stream<Class<?>> units) {
		try {
			final Map<String, UnitType> result = new HashMap<>();
			for (Iterator<?> iterator = units.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboUnit.class)) {
					RoboUnit anno = clazz.getAnnotation(RoboUnit.class);
					SimpleLoggingUtil.debug(getClass(), "ref->unit= " + anno.id());
					result.put(anno.id(), (UnitType) clazz.newInstance());
				}
			}
			return result;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ClientException("UNIT CACHE PROBLEM", e);
		}
	}

	@SuppressWarnings(value = "unchecked")
	private <ServiceType extends GenericService> Map<String, ServiceType> initServiceCache(Stream<Class<?>> services) {
		try {
			final Map<String, ServiceType> result = new HashMap<>();
			for (Iterator<?> iterator = services.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboService.class)) {
					RoboService anno = clazz.getAnnotation(RoboService.class);
					SimpleLoggingUtil.debug(getClass(), "ref->service= " + anno.id());
					result.put(anno.id(), (ServiceType) clazz.newInstance());
				}
			}
			return result;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ClientException("SERVICE CACHE PROBLEM", e);
		}
	}

	@SuppressWarnings(value = "unchecked")
	private <ProviderType extends BaseRegistryProvider> Map<String, ProviderType> initProviderCache(
			Stream<Class<?>> providers) {
		try {
			final Map<String, ProviderType> result = new HashMap<>();
			for (Iterator<?> iterator = providers.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboProvider.class)) {
					RoboProvider anno = clazz.getAnnotation(RoboProvider.class);
					result.put(anno.id(), (ProviderType) clazz.newInstance());
					SimpleLoggingUtil.debug(getClass(), "ref->provider= " + anno.id());
				}
			}
			return result;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ClientException("SERVICE PROVIDER PROBLEM", e);
		}
	}

}
