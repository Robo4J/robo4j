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
package com.robo4j.units.rpi.lcd;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;
import com.robo4j.hw.rpi.i2c.adafruitlcd.LcdFactory;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.AdafruitLcdImpl.Direction;
import com.robo4j.hw.rpi.utils.I2cBus;
import com.robo4j.units.rpi.I2CEndPoint;
import com.robo4j.units.rpi.I2CRegistry;
import com.robo4j.units.rpi.I2CRoboUnit;
import com.robo4j.util.StringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link RoboUnit} for the Adafruit 16x2 character LCD shield.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitLcdUnit extends I2CRoboUnit<LcdMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdafruitLcdUnit.class);
    private static final String ATTRIBUTE_NAME_COLOR = "color";
    private static final String ATTRIBUTE_NAME_TEXT = "text";

    public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
            .unmodifiableCollection(Arrays.asList(DefaultAttributeDescriptor.create(String.class, ATTRIBUTE_NAME_TEXT),
                    DefaultAttributeDescriptor.create(Color.class, ATTRIBUTE_NAME_COLOR)));

    private final AtomicReference<String> stringMessage = new AtomicReference<>(StringConstants.EMPTY);
    private AdafruitLcd lcd;

    public AdafruitLcdUnit(RoboContext context, String id) {
        super(LcdMessage.class, context, id);
    }

    /**
     * @param bus     used bus
     * @param address desired address
     */
    static AdafruitLcd getLCD(I2cBus bus, int address) throws IOException {
        Object lcd = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(bus, address));
        if (lcd == null) {
            try {
                lcd = LcdFactory.createLCD(bus, address);
                // Note that we cannot catch hardware specific exceptions here,
                // since they will be loaded when we run as mocked.
            } catch (Exception e) {
                throw new IOException(e);
            }
            I2CRegistry.registerI2CDevice(lcd, new I2CEndPoint(bus, address));
        }
        return (AdafruitLcd) lcd;
    }

    /**
     * @param message the message received by this unit.
     */
    @Override
    public void onMessage(LcdMessage message) {
        try {
            processLcdMessage(message);
        } catch (Exception e) {
            LOGGER.error("Could not accept message:{}", message, e);
        }
    }

    /**
     * @param configuration - unit configuration
     * @throws ConfigurationException exception
     */
    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        super.onInitialization(configuration);
        try {
            lcd = getLCD(getBus(), getAddress());
        } catch (IOException e) {
            throw new ConfigurationException("Could not initialize LCD", e);
        }
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        try {
            lcd.clear();
            lcd.setDisplayEnabled(false);
            lcd.stop();
        } catch (IOException e) {
            throw new AdafruitException("Could not disconnect LCD", e);
        }
        setState(LifecycleState.STOPPED);
    }

    /**
     * @param message accepted message type
     * @throws IOException
     */
    private void processLcdMessage(LcdMessage message) throws IOException {
        switch (message.getType()) {
            case CLEAR -> lcd.clear();
            case DISPLAY_ENABLE -> {
                final boolean displayEnable = Boolean.parseBoolean(message.getText().trim());
                lcd.setDisplayEnabled(displayEnable);
            }
            case SCROLL -> {
                // TODO: consider enum as the constant
                switch (message.getText().trim()) {
                    case "left":
                        lcd.scrollDisplay(Direction.LEFT);
                        break;
                    case "right":
                        lcd.scrollDisplay(Direction.RIGHT);
                        break;
                    default:
                        LOGGER.warn("unknown scroll direction:{}", message.getText());
                        break;
                }
            }

            case SET_TEXT -> {
                if (message.getColor() != null) {
                    lcd.setBacklight(message.getColor());
                }
                if (message.getText() != null) {
                    String text = message.getText();
                    lcd.setText(text);
                    stringMessage.set(text);
                }
            }
            case STOP -> lcd.stop();
            default -> LOGGER.warn("demo not supported:{}", message.getType());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (ATTRIBUTE_NAME_TEXT.equals(attribute.attributeName())) {
            return (R) stringMessage.get();
        } else if (ATTRIBUTE_NAME_COLOR.equals(attribute.attributeName())) {
            try {
                return (R) lcd.getBacklight();
            } catch (IOException e) {
                LOGGER.error("Failed to read the color:{}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public Collection<AttributeDescriptor<?>> getKnownAttributes() {
        return KNOWN_ATTRIBUTES;
    }

    /**
     * Fill one of the first 8 CGRAM locations with custom characters. The location
     * parameter should be between 0 and 7 and pattern should provide an array of 8
     * bytes containing the pattern. e.g. you can design your custom character at
     * &lt;a
     * href=http://www.quinapalus.com/hd44780udg.html&gt; http://www.quinapalus.com/hd44780udg.html&lt;a/&gt; .
     * To show your custom character obtain the string representation for the
     * location e.g. String.format("custom char=%c", 0).
     *
     * @param location storage location for this character, between 0 and 7
     * @param pattern  array of 8 bytes containing the character's pattern
     * @throws IOException exception
     */
    public void createChar(final int location, final byte[] pattern) throws IOException {
        lcd.createChar(location, pattern);
    }
}
