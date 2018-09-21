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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.robo4j.util.StringConstants;

/**
 * Factory for creating configurations from XML and vice versa.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class XmlConfigurationFactory {
	private static final String ENCODING = "UTF-8";
	public static final String ELEMENT_CONFIG = "config";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_PATH = "path";
	public static final String ATTRIBUTE_METHOD = "method";
	private static final String TYPE_BOOLEAN = "boolean";
	private static final String TYPE_LONG = "long";
	private static final String TYPE_DOUBLE = "double";
	private static final String TYPE_FLOAT = "float";
	private static final String TYPE_INT = "int";
	private static final String TYPE_STRING = "String";
	private static final String ELEMENT_ROOT = "com.robo4j.root";
	private static final String ELEMENT_VALUE = "value";

	private static final Deque<ConfigurationBuilder> configStack = new ArrayDeque<>();

	/*
	 * Internal XML Handler class for parsing the XML.
	 */
	private static class ConfigurationHandler extends DefaultHandler {
		private ConfigurationBuilder rootBuilder = new ConfigurationBuilder();
		private ConfigurationBuilder currentBuilder;
		private String lastElement = StringConstants.EMPTY;

		private String currentValue = StringConstants.EMPTY;
		private String currentType;
		private String currentName;

		ConfigurationHandler() {
			currentBuilder = rootBuilder;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals(ELEMENT_CONFIG)) {
				String value = attributes.getValue(ATTRIBUTE_NAME);
				if (!value.equals(ELEMENT_ROOT)) {
					configStack.push(currentBuilder);
					ConfigurationBuilder tmpBuilder = new ConfigurationBuilder();
					currentBuilder.addBuilder(value, tmpBuilder);
					currentBuilder = tmpBuilder;
				}
			} else if (qName.equals(ELEMENT_VALUE)) {
				currentName = attributes.getValue(ATTRIBUTE_NAME);
				currentType = attributes.getValue(ATTRIBUTE_TYPE);
			}
			lastElement = qName;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (qName) {
			case ELEMENT_VALUE:
				writeValue(currentBuilder, currentValue.trim(), currentType, currentName);
				currentValue = StringConstants.EMPTY;
				break;
			case ELEMENT_CONFIG:
				if (!configStack.isEmpty()) { // Closing of the last config...
					currentBuilder = configStack.pop();
				}
				break;
			}
			lastElement = StringConstants.EMPTY;
		}

		private static void writeValue(ConfigurationBuilder aConfigBuilder, String currentValue, String currentType, String currentName) {
			switch (currentType) {
			case TYPE_STRING:
				aConfigBuilder.addString(currentName, currentValue);
				break;
			case TYPE_INT:
				aConfigBuilder.addInteger(currentName, Integer.decode(currentValue));
				break;
			case TYPE_FLOAT:
				aConfigBuilder.addFloat(currentName, Float.parseFloat(currentValue));
				break;
			case TYPE_LONG:
				aConfigBuilder.addLong(currentName, Long.decode(currentValue));
				break;
			case TYPE_DOUBLE:
				aConfigBuilder.addDouble(currentName, Double.parseDouble(currentValue));
				break;
			case TYPE_BOOLEAN:
				aConfigBuilder.addBoolean(currentName, Boolean.parseBoolean(currentValue));
				break;

			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// NOTE(Marcus/Jan 22, 2017): Seems these can be called repeatedly
			// for a single text() node.
			switch (lastElement) {
			case ELEMENT_VALUE:
				currentValue += String.valueOf(ch, start, length);
				break;
			default:
				break;
			}
		}
		
		public ConfigurationBuilder getBuilder() {
			return rootBuilder;
		}

	}

	/**
	 * Reads a {@link Configuration} from a String.
	 * 
	 * @param xml
	 *            the String containing the XML to read.
	 * @return the {@link Configuration}.
	 * @throws ConfigurationFactoryException
	 *             if there was a problem reading the configuration.
	 */
	public static Configuration fromXml(String xml) throws ConfigurationFactoryException {
		SAXParser saxParser;
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
			 ConfigurationHandler handler = new ConfigurationHandler();
			saxParser.parse(new ByteArrayInputStream(xml.getBytes(ENCODING)), handler);
			return handler.getBuilder().build();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ConfigurationFactoryException("Could not parse the configuration", e);
		}
	}

	/**
	 * Serializes a {@link Configuration} to XML in a String object.
	 * 
	 * @param configuration
	 *            the {@link Configuration} to serialize to XML.
	 * @return the String containing the XML.
	 */
	public static String toXml(Configuration configuration) {
		StringBuilder builder = new StringBuilder();
		write(builder, configuration);
		// Walk the tree, add String
		return builder.toString();
	}

	private static void write(StringBuilder builder, Configuration configuration) {
		write(builder, configuration, ELEMENT_ROOT, 0);
	}

	private static void write(StringBuilder builder, Configuration configuration, String name, int level) {
		String indent = getIndentation(level);
		builder.append(String.format("%s<%s %s=\"%s\">\n", indent, ELEMENT_CONFIG, ATTRIBUTE_NAME, name));
		for (String valueName : configuration.getValueNames()) {
			writeValue(builder, valueName, configuration.getValue(valueName, null), level + 1);
		}
		for (String childName : configuration.getChildNames()) {
			write(builder, configuration.getChildConfiguration(childName), childName, level + 1);
		}
		builder.append(indent);
		builder.append(String.format("</%s>\n", ELEMENT_CONFIG));
	}

	private static String getIndentation(int level) {
		String result = StringConstants.EMPTY;
		for (int i = 0; i < level; i++) {
			result += "   ";
		}
		return result;
	}

	private static void writeValue(StringBuilder builder, String valueName, Object value, int level) {
		builder.append(String.format("%s<%s %s=\"%s\" %s=\"%s\">", getIndentation(level), ELEMENT_VALUE, ATTRIBUTE_NAME, valueName,
				ATTRIBUTE_TYPE, getTypeDescriptor(value)));
		builder.append(String.valueOf(value));
		builder.append(String.format("</%s>\n", ELEMENT_VALUE));
	}

	private static String getTypeDescriptor(Object value) {
		if (value instanceof String) {
			return TYPE_STRING;
		}
		if (value instanceof Integer) {
			return TYPE_INT;
		}
		if (value instanceof Float) {
			return TYPE_FLOAT;
		}
		if (value instanceof Double) {
			return TYPE_DOUBLE;
		}
		if (value instanceof Long) {
			return TYPE_LONG;
		}
		if (value instanceof Boolean) {
			return TYPE_BOOLEAN;
		}
		return null;
	}
}
