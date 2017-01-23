/*
 * Copyright (c) 2016, 2017, Miroslav Wengner, Marcus Hirt
 * This Robo4jBrick.java  is part of robo4j.
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
package com.robo4j.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * Builds a RoboSystem from various different sources.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboBuilder {
	private final Set<RoboUnit<?>> units = new HashSet<>();
	private final RoboSystem system = new RoboSystem();

	// FIXME(Marcus/Jan 22, 2017): Move to memento style typed property trees
	// and clean up parsing.
	// TODO, FIXME : move away the internal class
	private class RoboXMLHandler extends DefaultHandler {
		private String currentId = "";
		private String currentClassName = "";
		private Map<String, String> currentProperties;
		private String currentKey = "";
		private String currentValue = "";
		private String lastElement = "";

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (qName) {
			case "roboUnit":
				currentId = attributes.getValue("id");
				break;
			case "properties":
				currentProperties = new HashMap<>();
				break;
			}
			lastElement = qName;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (qName) {
			case "roboUnit":
				if (!verifyUnit()) {
					clearCurrentVariables();
				} else {
					try {
						SimpleLoggingUtil.debug(getClass(), "Loading " + currentClassName.trim() + " id=" + currentId);
						@SuppressWarnings("unchecked")
						Class<RoboUnit<?>> roboUnitClass = (Class<RoboUnit<?>>) Thread.currentThread()
								.getContextClassLoader().loadClass(currentClassName.trim());
						internalAddUnit(instantiateAndInitialize(roboUnitClass, currentId.trim(), currentProperties));
					} catch (Exception e) {
						throw new SAXException("Failed to parse robo unit", e);
					}
					clearCurrentVariables();
				}
				break;
			case "property":
				if (!validateProperty()) {
					clearCurrentProperty();
				} else {
					currentProperties.put(currentKey.trim(), currentValue.trim());
				}
				clearCurrentProperty();
			}
		}

		private boolean validateProperty() {
			if (currentKey.isEmpty()) {
				SimpleLoggingUtil.error(getClass(), "Error parsing unit property, no key. ID=" + currentId);
				return false;
			} else if (currentValue.isEmpty()) {
				SimpleLoggingUtil.error(getClass(), "Error parsing unit property, no value. ID=" + currentId);
				return false;
			}
			return true;
		}

		private void clearCurrentVariables() {
			currentId = "";
			currentClassName = "";
			currentProperties = null;
			clearCurrentProperty();
		}

		public void clearCurrentProperty() {
			currentKey = "";
			currentValue = "";
		}

		private boolean verifyUnit() {
			if (currentId.isEmpty()) {
				SimpleLoggingUtil.error(getClass(), "Error parsing unit, no ID");
				return false;
			} else if (currentClassName.isEmpty()) {
				SimpleLoggingUtil.error(getClass(), "Error parsing unit, no class name for " + currentId);
				return false;
			}
			return true;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// NOTE(Marcus/Jan 22, 2017): Seems these can be called repeatedly
			// for a single text() node.
			switch (lastElement) {
			case "key":
				currentKey += toString(ch, start, length);
				break;
			case "value":
				currentValue += toString(ch, start, length);
				break;
			case "class":
				currentClassName += toString(ch, start, length);
				break;
			default:
				break;
			}
		}

		private String toString(char[] data, int offset, int count) {
			return String.valueOf(data, offset, count);
		}

	}

	/**
	 * Adds a Robo4J unit to the builder.
	 * 
	 * @param unit
	 *            an initialized Robo4J unit. Must be initialized (see
	 *            {@link RoboUnit#initialize(Map)}) before added.
	 * 
	 * @return the builder.
	 * @throws RoboBuilderException
	 * 
	 * @see RoboUnit#initialize(java.util.Map)
	 */
	public RoboBuilder add(RoboUnit<?> unit) throws RoboBuilderException {
		internalAddUnit(unit);
		return this;
	}
	
	/**
	 * Instantiates a RoboUnit from the provided class using the provided id and
	 * adds it. No initialization ({@link RoboUnit#initialize(Map)} will take
	 * place.
	 * 
	 * @param clazz
	 *            the class to use.
	 * @param id
	 *            the id for the instatiated RoboUnit.
	 * @return the builder, for chaining.
	 * @throws RoboBuilderException
	 *             if the creation or adding of the unit failed.
	 */
	public RoboBuilder add(Class<RoboUnit<?>> clazz, String id) throws RoboBuilderException {
		internalAddUnit(instantiateRoboUnit(clazz, id));
		return this;
	}

	/**
	 * Instantiates a RoboUnit from the provided class using the provided id, initializes it and
	 * adds it. 
	 * 
	 * @param clazz
	 *            the class to use.
	 * @param id
	 *            the id for the instatiated RoboUnit.
	 * @return the builder, for chaining.
	 * @throws RoboBuilderException
	 *             if the creation or adding of the unit failed.
	 */
	public RoboBuilder add(Class<RoboUnit<?>> clazz, Map<String, String> properties, String id) throws RoboBuilderException {
		internalAddUnit(instantiateAndInitialize(clazz, id,  properties));
		return this;
	}

	/**
	 * Returns the built {@link RoboContext}. This should be the final method
	 * called on the builder.
	 * 
	 * @return the RoboContext.
	 */
	public RoboContext build() {
		system.addUnits(units);
		return system;
	}

	/**
	 * Will load all the units from the definitions found in the XML file and
	 * add them to the builder.
	 * 
	 * @param initFile
	 *            the xml file containing the definitions.
	 * 
	 * @return the builder.
	 * @throws RoboBuilderException
	 */
	public RoboBuilder add(InputStream inputStream) throws RoboBuilderException {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inputStream, new RoboXMLHandler());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RoboBuilderException("Could not initialize from xml", e);
		}
		return this;
	}
	
	// FIXME(Marcus/Jan 22, 2017): Implement.
	public RoboBuilder add(ClassLoader loader) throws RoboBuilderException {
		throw new UnsupportedOperationException("Not yet supported");
	}

	// FIXME(Marcus/Jan 22, 2017): Implement.
	public RoboBuilder add(ClassLoader loader, String match) throws RoboBuilderException {
		throw new UnsupportedOperationException("Not yet supported");
	}

	private void internalAddUnit(RoboUnit<?> unit) throws RoboBuilderException {
		if (unit == null) {
			throw new RoboBuilderException("Cannot add the null unit! Skipping");
		} else if (units.contains(unit)) {
			throw new RoboBuilderException("Only one unit with the id " + unit.getId()
					+ " can be active at a time. Skipping " + unit.toString());
		}
		units.add(unit);
	}
		
	private RoboUnit<?> instantiateRoboUnit(Class<RoboUnit<?>> clazz, String id) throws RoboBuilderException {
		try {
			Constructor<RoboUnit<?>> constructor = clazz.getConstructor(RoboContext.class, String.class);
			return constructor.newInstance(system, id);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new RoboBuilderException("Could not instantiate robo unit.", e);
		}
	}
	
	private RoboUnit<?> instantiateAndInitialize(Class<RoboUnit<?>> clazz, String id, Map<String, String> properties)
			throws RoboBuilderException {
		RoboUnit<?> unit = instantiateRoboUnit(clazz, id);
		if (properties != null && !properties.isEmpty()) {
			try {
				unit.initialize(properties);
			} catch (Exception e) {
				throw new RoboBuilderException("Error initializing RoboUnit", e);
			}
		}
		return unit;
	}
}
