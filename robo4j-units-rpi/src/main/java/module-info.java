module robo4j.units.rpi {
    requires robo4j.core;
    requires robo4j.math;
    requires robo4j.hw.rpi;

    requires transitive com.pi4j;

    exports com.robo4j.units.rpi.accelerometer;
    exports com.robo4j.units.rpi.camera;
    exports com.robo4j.units.rpi.gps;
    exports com.robo4j.units.rpi.gyro;
    exports com.robo4j.units.rpi.imu;
    exports com.robo4j.units.rpi.lcd;
    exports com.robo4j.units.rpi.led;
    exports com.robo4j.units.rpi.lidarlite;
    exports com.robo4j.units.rpi.pad;
    exports com.robo4j.units.rpi.pwm;
    exports com.robo4j.units.rpi.roboclaw;
}