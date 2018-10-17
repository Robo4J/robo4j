#!/bin/bash
#
# Handy environment variables for working with Robo4J on the RaspberryPi
# Set ROBO4J_HOME and ROBO4J_VERSION to the appropriate values
#
export ROBO4J_HOME=/home/pi/git/robo4j
export ROBO4J_VERSION=0.4
export PI4J_HOME=/opt/pi4j/lib
#
# No changes should be required below
#
export ROBO4J_PATH=$PI4J_HOME/*:$ROBO4J_HOME/robo4j-math/build/libs/robo4j-math-alpha-$ROBO4J_VERSION.jar:$ROBO4J_HOME/robo4j-core/build/libs/robo4j-core-alpha-$ROBO4J_VERSION.jar:$ROBO4J_HOME/robo4j-hw-rpi/build/libs/robo4j-hw-rpi-alpha-$ROBO4J_VERSION.jar:$ROBO4J_HOME/robo4j-units-rpi/build/libs/robo4j-units-rpi-alpha-$ROBO4J_VERSION.jar:$ROBO4J_HOME/robo4j-socket-http/build/libs/robo4j-socket-http-alpha-$ROBO4J_VERSION.jar
echo Robo4J v$ROBO4J_VERSION
echo ROBO4J_PATH=$ROBO4J_PATH