#!/usr/bin/env bash
echo "run LF710PadExample module-path..."
java -version
java --module-path . --module robo4j.hw.rpi/com.robo4j.hw.rpi.pad.LF710PadExample $@