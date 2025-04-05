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
package com.robo4j.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of a {@link Configuration}.
 *
 * <p>
 * Internal Use Only
 * </p>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class DefaultConfiguration implements Configuration {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfiguration.class);
    private final Map<String, Object> settings = new HashMap<>();
    private final Map<String, Configuration> configurations = new HashMap<>();

    @Override
    public Configuration getChildConfiguration(String name) {
        return configurations.get(name);
    }

    @Override
    public Double getDouble(String name, Double defaultValue) {
        return (Double) getVal(name, defaultValue);
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        return (Long) getVal(name, defaultValue);
    }

    @Override
    public String getString(String name, String defaultValue) {
        LOGGER.info("getString: name={}, defaultValue={}", name, defaultValue);
        return (String) getVal(name, defaultValue);
    }

    public void setString(String name, String s) {
        settings.put(name, s);
    }

    @Override
    public Character getCharacter(String name, Character defaultValue) {
        return (Character) getVal(name, defaultValue);
    }

    @Override
    public Integer getInteger(String name, Integer defaultValue) {
        return (Integer) getVal(name, defaultValue);
    }

    @Override
    public Float getFloat(String name, Float defaultValue) {
        return (Float) getVal(name, defaultValue);
    }

    @Override
    public Set<String> getValueNames() {
        return settings.keySet();
    }

    @Override
    public Set<String> getChildNames() {
        return configurations.keySet();
    }

    @Override
    public Object getValue(String name, Object defaultValue) {
        return getVal(name, defaultValue);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        return (Boolean) getVal(name, defaultValue);
    }

    @Override
    public boolean isDefined() {
        return settings.isEmpty() && configurations.isEmpty();
    }

    // TODO: review usage
    public Configuration createChildConfiguration(String name) {
        DefaultConfiguration config = (DefaultConfiguration) EMPTY_CONFIGURATION;
        configurations.put(name, config);
        return config;
    }

    public void setBoolean(String name, Boolean b) {
        settings.put(name, b);
    }

    public void setCharacter(String name, Character s) {
        settings.put(name, s);
    }

    public void setLong(String name, Long l) {
        settings.put(name, l);
    }

    public void setDouble(String name, Double d) {
        settings.put(name, d);
    }

    public void setInteger(String name, Integer i) {
        settings.put(name, i);
    }

    public void setFloat(String name, Float f) {
        settings.put(name, f);
    }

    /*
     * Package local, to be used by the builder.
     */
    void addChildConfiguration(String name, Configuration config) {
        configurations.put(name, config);
    }

    private Object getVal(String name, Object defaultValue) {
        Object val = settings.get(name);
        if (val == null) {
            return defaultValue;
        }
        return val;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DefaultConfiguration that = (DefaultConfiguration) object;
        return Objects.equals(settings, that.settings) && Objects.equals(configurations, that.configurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settings, configurations);
    }

    @Override
    public String toString() {
        return "Settings: " + new HashMap<>(this.settings) + " Configurations: " + this.configurations.size();
    }
}
