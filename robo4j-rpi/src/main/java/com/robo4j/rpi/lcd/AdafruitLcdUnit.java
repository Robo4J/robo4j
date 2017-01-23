package com.robo4j.rpi.lcd;

import java.io.IOException;
import java.util.Map;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.robo4j.commons.annotation.Unit;
import com.robo4j.commons.io.RoboResult;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.io.RoboContext;
import com.robo4j.commons.unit.RoboUnit;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.LcdFactory;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLcd.Direction;
import com.robo4j.rpi.I2CEndPoint;
import com.robo4j.rpi.I2CRegistry;

@Unit
public class AdafruitLcdUnit extends RoboUnit<String> {
	private AdafruitLcd lcd;

	public AdafruitLcdUnit(RoboContext context, String id) {
		super(context, id);
	}

	static AdafruitLcd getLCD(int bus, int address) throws IOException, UnsupportedBusNumberException {
		Object lcd = I2CRegistry.getI2CDeviceByEndPoint(new I2CEndPoint(bus, address));
		if (lcd == null) {
			lcd = LcdFactory.createLCD(bus, address);
			I2CRegistry.registerI2CDevice(lcd, new I2CEndPoint(bus, address));
		}
		return (AdafruitLcd) lcd;
	}

	@Override
	public void initialize(Map<String, String> properties) throws Exception {
		super.initialize(properties);
		int bus = Integer.parseInt(properties.get("bus"));
		int address = Integer.parseInt(properties.get("address"));

		lcd = getLCD(bus, address);
	}

	@Override
	public RoboResult<?> onMessage(Object message) {
		try {

			if (message instanceof LcdMessage) {
				processLcdMessage((LcdMessage) message);
			} else if (message instanceof String) {
				lcd.setText((String) message);
			}
		} catch (Exception e) {
			SimpleLoggingUtil.debug(getClass(), "Could not accept message" + message.toString(), e);
		}

		return super.onMessage(message);
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	private void processLcdMessage(LcdMessage message) throws IOException {
		switch (message.getType()) {
		case CLEAR:
			lcd.clear();
			break;
		case DISPLAY_ENABLE:
			String enablement = message.getText();
			if (enablement.equals("true")) {
				lcd.setDisplayEnabled(true);
			} else if (enablement.equals("false")) {
				lcd.setDisplayEnabled(false);
			} else {
				SimpleLoggingUtil.error(getClass(), "Display enablement " + enablement + " is unknown");
			}
			break;
		case SCROLL:
			String direction = message.getText();
			if (direction.equals("left")) {
				lcd.scrollDisplay(Direction.LEFT);
			} else if (direction.equals("right")) {
				lcd.scrollDisplay(Direction.RIGHT);
			} else {
				SimpleLoggingUtil.error(getClass(), "Scroll direction " + direction + " is unknown");
			}
			break;
		case SET_TEXT:
			String text = message.getText();
			if (text != null) {
				lcd.setText(text);
			}
			break;
		case STOP:
			lcd.stop();
			break;
		default:
			SimpleLoggingUtil.error(getClass(), message.getType() + " not supported!");
			break;
		}
	}
}
