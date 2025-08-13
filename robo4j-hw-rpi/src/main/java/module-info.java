
module robo4j.hw.rpi {
    opens com.robo4j.hw.rpi to com.pi4j.plugin.linuxfs;

    requires transitive robo4j.math;
    requires jdk.jfr;
    requires java.desktop;
    requires com.pi4j;
    requires com.pi4j.plugin.linuxfs;
    requires org.slf4j;

    exports com.robo4j.hw.rpi;
    exports com.robo4j.hw.rpi.camera;
    exports com.robo4j.hw.rpi.gps;
    exports com.robo4j.hw.rpi.i2c;
    exports com.robo4j.hw.rpi.i2c.accelerometer;
    exports com.robo4j.hw.rpi.i2c.adafruitbackpack;
    exports com.robo4j.hw.rpi.i2c.adafruitlcd;
    exports com.robo4j.hw.rpi.i2c.adafruitlcd.impl;
    exports com.robo4j.hw.rpi.i2c.adafruitoled;
    exports com.robo4j.hw.rpi.i2c.bmp;
    exports com.robo4j.hw.rpi.i2c.gps;
    exports com.robo4j.hw.rpi.i2c.gyro;
    exports com.robo4j.hw.rpi.i2c.lidar;
    exports com.robo4j.hw.rpi.i2c.magnetometer;
    exports com.robo4j.hw.rpi.i2c.pwm;
    exports com.robo4j.hw.rpi.imu.bno;
    exports com.robo4j.hw.rpi.imu.bno.impl;
    exports com.robo4j.hw.rpi.imu.bno.shtp;
    exports com.robo4j.hw.rpi.lcd;
    exports com.robo4j.hw.rpi.pad;
    exports com.robo4j.hw.rpi.pwm;
    exports com.robo4j.hw.rpi.pwm.roboclaw;
    exports com.robo4j.hw.rpi.serial.gps;
    exports com.robo4j.hw.rpi.utils;

    uses com.robo4j.math.geometry.impl.ScanResultImpl;
    uses java.awt.BorderLayout;
    uses java.awt.Component;
    uses java.awt.event.MouseAdapter;
    uses java.awt.event.MouseEvent;
    uses java.nio.Buffer;
}
