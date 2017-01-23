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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.robo4j.core.annotation.RoboMotor;
import com.robo4j.core.annotation.RoboProvider;
import com.robo4j.core.annotation.RoboSensor;
import com.robo4j.core.annotation.RoboService;
import com.robo4j.core.annotation.RoboUnit;
import com.robo4j.core.annotation.RoboUnitProducer;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.motor.GenericMotor;
import com.robo4j.core.registry.BaseRegistryProvider;
import com.robo4j.core.registry.RegistryManager;
import com.robo4j.core.registry.RegistryTypeEnum;
import com.robo4j.core.sensor.GenericSensor;
import com.robo4j.core.service.GenericService;
import com.robo4j.core.unit.GenericUnit;
import com.robo4j.core.unit.UnitProducer;
import com.robo4j.core.client.io.ClientException;

/**
 * Initial implementation of robo-client reflection usage
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 16.06.2016
 */

class RoboReflectiveInit {

	@SuppressWarnings(value = "unchecked")
	RoboReflectiveInit(Map<Class<? extends Annotation>,  Stream<Class<?>>> coreMap) {


		/* initiation of all caches used at the run time */
		ExecutorService executors = Executors.newFixedThreadPool(RegistryUtil.registry.size());
		Future<Map<String, GenericMotor>> futureEngineCache =
				executors.submit(() -> initEngineCache(coreMap.get(RoboMotor.class)));
		Future<Map<String, GenericSensor>> futureSensorsCache =
				executors.submit(() -> initSensorCache(coreMap.get(RoboSensor.class)));
		Future<Map<String, UnitProducer>> futureUnitProducersCache =
				executors.submit(() -> initUnitProducerCache(coreMap.get(RoboUnitProducer.class)));
		Future<Map<String, GenericUnit>> futureUnitsCache =
				executors.submit(() -> initUnitCache(coreMap.get(RoboUnit.class)));
		Future<Map<String, GenericService>> futureServicesCache =
				executors.submit(() -> initServiceCache(coreMap.get(RoboService.class)));
		Future<Map<String, BaseRegistryProvider>> futureProviderCache = executors
				.submit(() -> initProviderCache(coreMap.get(RoboProvider.class)));

		try {
			final Map<String, GenericMotor> initEngines = futureEngineCache.get();
			final Map<String, GenericSensor> initSensors = futureSensorsCache.get();
			final Map<String, UnitProducer> initUnitProducers = futureUnitProducersCache.get();
			final Map<String, GenericUnit> initUnits = futureUnitsCache.get();
			final Map<String, GenericService> initServices = futureServicesCache.get();
			final Map<String, BaseRegistryProvider> initProviders = futureProviderCache.get();
			RegistryManager registryManager = RegistryManager.getInstance().addAll(RegistryUtil.registry);
			if (initEngines != null && initSensors != null && initUnitProducers != null
					&&initUnits != null && initServices != null
					&& initProviders != null ) {

				SimpleLoggingUtil.debug(getClass(), "RegistryManager");
				if (registryManager.isActive()) {
					//TODO: can be simplified

					registryManager.initRegistry(RegistryTypeEnum.PROVIDER, futureProviderCache.get());
					registryManager.initRegistry(RegistryTypeEnum.ENGINES, initEngines);
					registryManager.initRegistry(RegistryTypeEnum.SENSORS, initSensors);
					registryManager.initRegistry(RegistryTypeEnum.UNIT_PRODUCERS, initUnitProducers);
					SimpleLoggingUtil.debug(getClass(), "InitRegistry initUnitProducers DONE");
					registryManager.initRegistry(RegistryTypeEnum.UNITS, initUnits);
					registryManager.initRegistry(RegistryTypeEnum.SERVICES, initServices);
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

	// TODO: FIMXE -> create logic to check hardware resources
	@SuppressWarnings(value = "unchecked")
	private <UnitProducerType extends UnitProducer> Map<String, UnitProducerType> initUnitProducerCache(Stream<Class<?>> producer){
		try {
			final Map<String, UnitProducerType> result = new HashMap<>();
			for (Iterator<?> iterator = producer.iterator(); iterator.hasNext();) {
				Class<?> clazz = (Class<?>) iterator.next();
				if (clazz.isAnnotationPresent(RoboUnitProducer.class)) {
					RoboUnitProducer anno = clazz.getAnnotation(RoboUnitProducer.class);
					result.put(anno.id(), (UnitProducerType) clazz.newInstance());
					SimpleLoggingUtil.debug(getClass(), "ref->unitProducer= " + anno.id());
				}
			}
			return result;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ClientException("Producer CACHE PROBLEM", e);
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
