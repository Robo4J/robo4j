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

# Pre-defined debug flags
export DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=*:9876,suspend=y"

# Pre-defined JFR flags
export JFR="-XX:StartFlightRecording=settings=profile,dumponexit=true,filename=$ROBO4J_HOME/latestrun.jfr"

#
# No changes should be required below
#
function createRobo4jPath(){
    local buildLibs="build/libs"
    local moduleList=("robo4j-math" "robo4j-core" "robo4j-hw-rpi" "robo4j-units-rpi" "robo4j-socket-http")
    local robo4jPath="$PI4J_HOME/*"
    for moduleName in "${moduleList[@]}"
    do
        robo4jPath+=":$ROBO4J_HOME/$moduleName/$buildLibs/$moduleName-$ROBO4J_VERSION-SNAPSHOT.jar"
    done
    echo "$robo4jPath"
}

export ROBO4J_PATH=$(createRobo4jPath)
export _JAVA_OPTIONS="--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED"
echo ROBO4J v$ROBO4J_VERSION
echo ROBO4J_PATH=$ROBO4J_PATH
echo DEBUG=$DEBUG
echo JFR=$JFR
