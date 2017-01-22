package com.robo4j.rpi.lcd;

import java.io.IOException;
import java.util.Map;

import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonListener;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonPressedObserver;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;

public class ButtonUnit extends RoboUnit<Object> {
	public static enum Messages {
		UP("U"), DOWN("D"), LEFT("L"), RIGHT("R"), SELECT("S");

		private String command;

		private Messages(String command) {
			this.command = command;
		}

		public String getMessage() {
			return command;
		}

		public Messages fromCommand(String command) {
			switch (command) {
			case "U":
				return UP;
			case "D":
				return DOWN;
			case "L":
				return LEFT;
			case "R":
				return RIGHT;
			default:
				return SELECT;
			}
		}
	}

	private AdafruitLcd lcd;
	private ButtonPressedObserver observer;
	private String target;
	private ButtonListener buttonListener;

	public ButtonUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	public void initialize(Map<String, String> properties) throws Exception {
		super.initialize(properties);
		int bus = Integer.parseInt(properties.get("bus"));
		int address = Integer.parseInt(properties.get("address"));
		target = properties.get("target");
		lcd = AdafruitLcdUnit.getLCD(bus,address);
	}



	public void start() {
		setState(LifecycleState.STARTING);
		observer = new ButtonPressedObserver(lcd);
		buttonListener = new ButtonListener() {
			@Override
			public void onButtonPressed(Button button) {
				if (getState() == LifecycleState.STARTED) {
					try {
						switch (button) {
						case UP:
							sendMessage(target, Messages.UP);
							break;
						case DOWN:
							sendMessage(target, Messages.DOWN);
							break;
						case RIGHT:
							sendMessage(target, Messages.LEFT);
							break;
						case LEFT:
							sendMessage(target, Messages.RIGHT);
							break;
						case SELECT:
							sendMessage(target, Messages.SELECT);
							break;
						default:
							lcd.clear();
							lcd.setText(String.format("Button %s\nis not in use...", button.toString()));
						}
					} catch (IOException e) {
						handleException(e);
					}					
				}
			}
		};
		observer.addButtonListener(buttonListener);
		setState(LifecycleState.STARTED);
	}
	
	public void stop() {
		observer.removeButtonListener(buttonListener);
		observer = null;
		buttonListener = null;
	}
	
	public void shutdown() {
		try {
			lcd.stop();
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "Failed to stop LCD", e);
		}
	}

	private void handleException(IOException e) {
		setState(LifecycleState.STOPPING);
		shutdown();
		setState(LifecycleState.FAILED);
	}
}
