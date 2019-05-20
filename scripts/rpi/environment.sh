#!/bin/bash
#
# Handy environment variables for working with Robo4J on the RaspberryPi
# Set ROBO4J_HOME and ROBO4J_VERSION to the appropriate values
#
export ROBO4J_HOME=/home/pi/git/robo4j
export ROBO4J_VERSION=0.5
#export PI4J_HOME=/opt/pi4j/lib
# Using the 2.0 SNAPSHOT until released
export PI4J_HOME=~/.m2/repository/com/pi4j/pi4j-core/2.0-SNAPSHOT/

#
# No changes should be required below
#
export ROBO4J_PATH=$PI4J_HOME/*:$ROBO4J_HOME/robo4j-math/build/libs/robo4j-math-$ROBO4J_VERSION-SNAPSHOT.jar:$ROBO4J_HOME/robo4j-core/build/libs/robo4j-core-$ROBO4J_VERSION-SNAPSHOT.jar:$ROBO4J_HOME/robo4j-hw-rpi/build/libs/robo4j-hw-rpi-$ROBO4J_VERSION-SNAPSHOT.jar:$ROBO4J_HOME/robo4j-units-rpi/build/libs/robo4j-units-rpi-$ROBO4J_VERSION-SNAPSHOT.jar:$ROBO4J_HOME/robo4j-socket-http/build/libs/robo4j-socket-http-$ROBO4J_VERSION-SNAPSHOT.jar
export _JAVA_OPTIONS="--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED"
echo Robo4J v$ROBO4J_VERSION
echo ROBO4J_PATH=$ROBO4J_PATH
