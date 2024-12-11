/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.configuration.ConfigurationFactoryException;
import com.robo4j.configuration.XmlConfigurationFactory;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.util.StringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds a RoboSystem from various different sources.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboBuilder.class);
    /**
     * Configuration key for the maximum thread size for the scheduler thread
     * pool.
     */
    public static final String KEY_SCHEDULER_POOL_SIZE = "poolSizeScheduler";
    /**
     * Configuration key for the maximum thread size for the worker thread pool.
     */
    public static final String KEY_WORKER_POOL_SIZE = "poolSizeWorker";
    /**
     * Configuration key for the maximum thread size for the worker thread pool.
     */
    public static final String KEY_BLOCKING_POOL_SIZE = "poolSizeBlocking";
    /**
     * Configuration key for the child configuration for the message server.
     */
    public static final String KEY_CONFIGURATION_SERVER = "com.robo4j.messageServer";
    /**
     * Configuration key for the child configuration for the auto discovery.
     */
    public static final String KEY_CONFIGURATION_EMITTER = "com.robo4j.discovery";
    /**
     * Configuration key for the auto discovery metadata service.
     */
    public static final String KEY_CONFIGURATION_EMITTER_METADATA = "com.robo4j.discovery.metadata";

    private final Set<RoboUnit<?>> units = new HashSet<>();
    private final RoboSystem system;

    /**
     * Class responsible for the XML parsing. Using SAX to keep down resource
     * requirements.
     */
    private class RoboXMLHandler extends DefaultHandler {
        private static final String ELEMENT_ROBO_UNIT = "roboUnit";
        private String currentId = StringConstants.EMPTY;
        private String currentClassName = StringConstants.EMPTY;
        private String currentConfiguration = StringConstants.EMPTY;
        private String lastElement = StringConstants.EMPTY;
        private boolean configState = false;
        private boolean inSystemElement = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case ELEMENT_ROBO_UNIT -> currentId = attributes.getValue("id");
                case SystemXMLHandler.ELEMENT_SYSTEM -> inSystemElement = true;
                case XmlConfigurationFactory.ELEMENT_CONFIG -> {
                    if (!configState && !inSystemElement) {
                        currentConfiguration = StringConstants.EMPTY;
                        configState = true;
                    }
                }
            }
            lastElement = qName;
            if (configState) {
                currentConfiguration += String.format("<%s %s>", qName, toString(attributes));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(SystemXMLHandler.ELEMENT_SYSTEM)) {
                inSystemElement = false;
            } else if (qName.equals(ELEMENT_ROBO_UNIT)) {
                if (!verifyUnit()) {
                    clearCurrentVariables();
                } else {
                    try {
                        LOGGER.debug("Loading {} id={}", currentClassName.trim(), currentId);
                        @SuppressWarnings("unchecked")
                        Class<RoboUnit<?>> roboUnitClass = (Class<RoboUnit<?>>) Thread.currentThread().getContextClassLoader()
                                .loadClass(currentClassName.trim());
                        Configuration config = currentConfiguration.trim().equals(StringConstants.EMPTY) ? null
                                : XmlConfigurationFactory.fromXml(currentConfiguration);
                        internalAddUnit(instantiateAndInitialize(roboUnitClass, currentId.trim(), config));
                    } catch (Exception e) {
                        throw new SAXException("Failed to parse robo unit", e);
                    }
                    clearCurrentVariables();
                }
                configState = false;
            }
            if (configState) {
                currentConfiguration += String.format("</%s>", qName);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (configState) {
                currentConfiguration += toString(ch, start, length);
            }
            // NOTE(Marcus/Jan 22, 2017): Seems these can be called repeatedly
            // for a single text() node.
            switch (lastElement) {
                case XmlConfigurationFactory.ELEMENT_CONFIG -> currentConfiguration += toString(ch, start, length);
                case XmlConfigurationFactory.ELEMENT_CLASS -> currentClassName += toString(ch, start, length);
            }
        }

        private Object toString(Attributes attributes) {
            return String.format("%s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\"", XmlConfigurationFactory.ATTRIBUTE_NAME,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_NAME), XmlConfigurationFactory.ATTRIBUTE_TYPE,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_TYPE), XmlConfigurationFactory.ATTRIBUTE_PATH,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_PATH), XmlConfigurationFactory.ATTRIBUTE_METHOD,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_METHOD));
        }

        private void clearCurrentVariables() {
            currentId = StringConstants.EMPTY;
            currentClassName = StringConstants.EMPTY;
            currentConfiguration = StringConstants.EMPTY;
        }

        private boolean verifyUnit() {
            if (currentId.isEmpty()) {
                LOGGER.error("Error parsing unit, no ID");
                return false;
            } else if (currentClassName.isEmpty()) {
                LOGGER.error("Error parsing unit, no class name for {}", currentId);
                return false;
            }
            return true;
        }

        private String toString(char[] data, int offset, int count) {
            return String.valueOf(data, offset, count);
        }
    }

    /**
     * Separate handler for reading the system part.
     */
    private static class SystemXMLHandler extends DefaultHandler {
        private static final String ELEMENT_SYSTEM = "roboSystem";
        private String currentId = StringConstants.EMPTY;
        private String currentConfiguration = StringConstants.EMPTY;
        private String lastElement = StringConstants.EMPTY;
        private boolean configState = false;
        private RoboSystem system;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case ELEMENT_SYSTEM -> currentId = attributes.getValue("id");
                case XmlConfigurationFactory.ELEMENT_CONFIG -> {
                    if (!configState) {
                        currentConfiguration = StringConstants.EMPTY;
                        configState = true;
                    }
                }
            }
            lastElement = qName;
            if (configState) {
                currentConfiguration += String.format("<%s %s>", qName, toString(attributes));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(ELEMENT_SYSTEM)) {
                LOGGER.debug("Loading system id={}", currentId);
                Configuration config;
                try {
                    // TODO : review config = null
                    config = currentConfiguration.trim().isEmpty() ? null : XmlConfigurationFactory.fromXml(currentConfiguration);
                    if (currentId == null) {
                        system = new RoboSystem(config);
                    } else {
                        system = new RoboSystem(currentId, config);
                    }
                } catch (ConfigurationFactoryException e) {
                    LOGGER.error("Error parsing system", e);
                }
                configState = false;
            }

            if (configState) {
                currentConfiguration += String.format("</%s>", qName);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (configState) {
                currentConfiguration += toString(ch, start, length);
            }
            // NOTE(Marcus/Jan 22, 2017): Seems these can be called repeatedly
            // for a single text() node.
            if (lastElement.equals(XmlConfigurationFactory.ELEMENT_CONFIG)) {
                currentConfiguration += toString(ch, start, length);
            }
        }

        private Object toString(Attributes attributes) {
            return String.format("%s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\"", XmlConfigurationFactory.ATTRIBUTE_NAME,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_NAME), XmlConfigurationFactory.ATTRIBUTE_TYPE,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_TYPE), XmlConfigurationFactory.ATTRIBUTE_PATH,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_PATH), XmlConfigurationFactory.ATTRIBUTE_METHOD,
                    attributes.getValue(XmlConfigurationFactory.ATTRIBUTE_METHOD));
        }

        private String toString(char[] data, int offset, int count) {
            return String.valueOf(data, offset, count);
        }

        public RoboSystem createSystem() {
            return system == null ? new RoboSystem() : system;
        }
    }

    /**
     * Constructor.
     */
    public RoboBuilder() {
        system = new RoboSystem();
    }

    /**
     * Use this builder constructor to configure the system.
     *
     * @param systemConfig initial system configuration
     */
    public RoboBuilder(Configuration systemConfig) {
        system = new RoboSystem(systemConfig);
    }

    /**
     * Use this builder constructor to configure the system.
     *
     * @param uid          a manually configured unique identifier for the system
     * @param systemConfig initial system configuration
     */
    public RoboBuilder(String uid, Configuration systemConfig) {
        system = new RoboSystem(uid, systemConfig);
    }

    /**
     * Use this builder constructor to configure the RoboSystem.
     *
     * @param systemConfig the configuration settings for the system. Note that this is
     *                     separate from the unit definitions today.
     * @throws RoboBuilderException possible exception
     */
    public RoboBuilder(InputStream systemConfig) throws RoboBuilderException {
        system = readSystemFromXML(systemConfig);
    }

    /**
     * Adds a Robo4J unit to the builder.
     *
     * @param unit an initialized Robo4J unit. Must be initialized (see
     *             {@link RoboUnit#initialize(Configuration)}) before added.
     * @return the builder.
     * @throws RoboBuilderException possible exception
     * @see RoboUnit#initialize(Configuration)
     */
    public RoboBuilder add(RoboUnit<?> unit) throws RoboBuilderException {
        internalAddUnit(unit);
        return this;
    }

    /**
     * Adds a collection of Robo4J units to the builder.
     *
     * @param units a collection of units to add.
     * @return the builder.
     * @throws RoboBuilderException possible exception
     * @see RoboUnit#initialize(Configuration)
     */
    public RoboBuilder addAll(Collection<RoboUnit<?>> units) throws RoboBuilderException {
        for (RoboUnit<?> unit : units) {
            add(unit);
        }
        return this;
    }

    /**
     * Adds an array of Robo4J units to the builder.
     *
     * @param units a collection of units to add.
     * @return the builder.
     * @throws RoboBuilderException possible exception
     * @see RoboUnit#initialize(Configuration)
     */
    public RoboBuilder addAll(RoboUnit<?>... units) throws RoboBuilderException {
        for (RoboUnit<?> unit : units) {
            add(unit);
        }
        return this;
    }

    /**
     * Instantiates a RoboUnit from the provided class using the provided id and
     * adds it. The instance will be initialized using an empty configuration.
     *
     * @param clazz the class to use.
     * @param id    the id for the instantiated RoboUnit.
     * @return the builder, for chaining.
     * @throws RoboBuilderException if the creation or adding of the unit failed.
     */
    public RoboBuilder add(Class<? extends RoboUnit<?>> clazz, String id) throws RoboBuilderException {
        internalAddUnit(instantiateAndInitialize(clazz, id, ConfigurationFactory.createEmptyConfiguration()));
        return this;
    }

    /**
     * Instantiates a RoboUnit from the provided class using the provided id,
     * initializes it and adds it.
     *
     * @param clazz         the class to use.
     * @param configuration RoboUnit configuration
     * @param id            the id for the instantiated RoboUnit.
     * @return the builder, for chaining.
     * @throws RoboBuilderException if the creation or adding of the unit failed.
     */
    public RoboBuilder add(Class<? extends RoboUnit<?>> clazz, Configuration configuration, String id) throws RoboBuilderException {
        internalAddUnit(instantiateAndInitialize(clazz, id, configuration));
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
        LookupServiceProvider.registerLocalContext(system);
        system.setState(LifecycleState.INITIALIZED);
        return system;
    }

    /**
     * Returns the context being built. This should only be used so that units
     * can be instantiated with more control outside the builder. Note that you
     * should then add the instances to the builder through add.
     *
     * @return the context being built.
     */
    public RoboContext getContext() {
        return system;
    }

    /**
     * Will load all the units from the definitions found in the XML file and
     * add them to the builder.
     *
     * @param inputStream the xml file containing the definitions.
     * @return the builder.
     * @throws RoboBuilderException possible exception
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
    // public RoboBuilder add(ClassLoader loader) throws RoboBuilderException {
    // throw new UnsupportedOperationException("Not yet supported");
    // }

    // FIXME(Marcus/Jan 22, 2017): Implement.
    // public RoboBuilder add(ClassLoader loader, String match) throws
    // RoboBuilderException {
    // throw new UnsupportedOperationException("Not yet supported");
    // }

    private void internalAddUnit(RoboUnit<?> unit) throws RoboBuilderException {
        if (unit == null) {
            throw new RoboBuilderException("Cannot add the null unit! Skipping");
        } else if (units.contains(unit)) {
            throw new RoboBuilderException(
                    "Only one unit with the id " + unit.id() + " can be active at a time. Skipping " + unit.toString());
        }
        units.add(unit);
    }

    private RoboUnit<?> instantiateRoboUnit(Class<? extends RoboUnit<?>> clazz, String id) throws RoboBuilderException {
        try {
            Constructor<? extends RoboUnit<?>> constructor = clazz.getConstructor(RoboContext.class, String.class);
            return constructor.newInstance(system, id);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                 IllegalArgumentException
                 | InvocationTargetException e) {
            throw new RoboBuilderException("Could not instantiate robo unit.", e);
        }
    }

    private RoboUnit<?> instantiateAndInitialize(Class<? extends RoboUnit<?>> clazz, String id, Configuration configuration)
            throws RoboBuilderException {
        RoboUnit<?> unit = instantiateRoboUnit(clazz, id);
        if (configuration != null) {
            try {
                unit.initialize(configuration);
            } catch (Exception e) {
                throw new RoboBuilderException("Error initializing RoboUnit", e);
            }
        }
        return unit;
    }

    private RoboSystem readSystemFromXML(InputStream stream) throws RoboBuilderException {
        try {
            SystemXMLHandler handler = new SystemXMLHandler();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(stream, handler);
            return handler.createSystem();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RoboBuilderException("Could not initialize system from xml", e);
        }
    }
}
