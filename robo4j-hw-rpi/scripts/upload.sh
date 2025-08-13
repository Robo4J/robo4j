#!/usr/bin/env bash
echo "upload Robo4j-hw-rpi example module-path..."
ssh pi@192.168.22.20  "rm /home/pi/jpms-robo4j-hw-rpi/*"
scp -r ../target/jpms-robo4j-hw-rpi pi@192.168.22.20:/home/pi