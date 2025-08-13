#!/usr/bin/env bash
echo "run AlphanumericDeviceExample module-path..."
java -version
java --module-path . --module robo4j.units.rpi/com.robo4j.units.rpi.led.AdafruitAlphanumericUnitMetricsExample $@